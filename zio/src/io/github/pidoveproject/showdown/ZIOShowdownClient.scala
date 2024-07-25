package io.github.pidoveproject.showdown

import io.github.pidoveproject.showdown.protocol.ProtocolError
import zio.*
import zio.http.*
import zio.stream.Stream

class ZIOShowdownClient(client: Client, serverUrl: String) extends ShowdownClient[WebSocketFrame, IO, Stream, [x] =>> ZIO[Scope, ProtocolError, x]]:

  override def openConnection: ZIO[Scope, ProtocolError, ZIOShowdownConnection] =
    def socketApp(promise: Promise[ProtocolError, ZIOShowdownConnection]) =
      Handler.webSocket(channel => promise.succeed(ZIOShowdownConnection(client, channel)))

    for
      url <- ZIO.fromEither(URL.decode(serverUrl)).toProtocolZIO
      connectionPromise <- Promise.make[ProtocolError, ZIOShowdownConnection]
      _ <- client.socket(url = url, headers = Headers.empty, app = socketApp(connectionPromise)).toProtocolZIO
      connection <- connectionPromise.await
    yield
      connection

object ZIOShowdownClient:

  def layer(serverUrl: String = "wss://sim3.psim.us/showdown/websocket"): ZLayer[Client, Nothing, ZIOShowdownClient] =
    ZLayer:
      for
        client <- ZIO.service[Client]
      yield
        ZIOShowdownClient(client, serverUrl)

  def openConnection: ZIO[ZIOShowdownClient & Scope, ProtocolError, ZIOShowdownConnection] =
    ZIO.serviceWithZIO[ZIOShowdownClient](_.openConnection)