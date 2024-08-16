package io.github.pidoveproject.showdown

import io.github.pidoveproject.showdown.protocol.ProtocolError
import zio.*
import zio.http.*
import zio.http.URL as ZURL
import zio.stream.Stream
import io.github.pidoveproject.showdown.protocol.URL
import io.github.iltotore.iron.autoRefine

class ZIOShowdownClient(client: Client)
    extends ShowdownClient[WebSocketFrame, IO, [r] =>> Stream[Throwable, r], [x] =>> ZIO[Scope, ProtocolError, x]]:

  override def openConnection(serverUrl: URL = URL("wss://sim3.psim.us/showdown/websocket")): ZIO[Scope, ProtocolError, ZIOShowdownConnection] =
    def socketApp(promise: Promise[ProtocolError, ZIOShowdownConnection]) =
      Handler.webSocket(channel => promise.succeed(ZIOShowdownConnection(client, channel)))

    for
      url <- ZIO.fromEither(ZURL.decode(serverUrl.value)).toProtocolZIO
      connectionPromise <- Promise.make[ProtocolError, ZIOShowdownConnection]
      _ <- client.socket(url = url, headers = Headers.empty, app = socketApp(connectionPromise)).toProtocolZIO
      connection <- connectionPromise.await
    yield connection

object ZIOShowdownClient:

  def layer: ZLayer[Client, Nothing, ZIOShowdownClient] =
    ZLayer:
      for
        client <- ZIO.service[Client]
      yield ZIOShowdownClient(client)

  def openConnection(serverUrl: URL = URL("wss://sim3.psim.us/showdown/websocket"))
      : ZIO[ZIOShowdownClient & Scope, ProtocolError, ZIOShowdownConnection] =
    ZIO.serviceWithZIO[ZIOShowdownClient](_.openConnection(serverUrl))
