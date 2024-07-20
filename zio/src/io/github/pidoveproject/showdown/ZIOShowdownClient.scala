package io.github.pidoveproject.showdown

import io.github.pidoveproject.showdown.protocol.ProtocolError
import zio.*
import zio.http.*
import zio.stream.Stream

class ZIOShowdownClient(client: Client, serverUrl: String) extends ShowdownClient[WebSocketFrame, IO, Stream]:

  override def openConnection(handler: ShowdownConnection[WebSocketFrame, IO, Stream] => IO[ProtocolError, Unit]): IO[ProtocolError, Unit] =

    ZIO.scoped:
      for
        aliveRef <- Ref.make(true)
        socketApp = Handler.webSocket(ZIOShowdownConnection.withHandler(client, aliveRef, handler))
        url <- ZIO.fromEither(URL.decode(serverUrl)).toProtocolZIO
        _ <- client.socket(url = url, headers = Headers.empty, app = socketApp).unit.toProtocolZIO
        _ <- ZIO.sleep(Duration.Zero).repeatWhileZIO(_ => aliveRef.get)
      yield
        ()

object ZIOShowdownClient:

  def layer(serverUrl: String = "wss://sim3.psim.us/showdown/websocket"): ZLayer[Client, Nothing, ShowdownClient[WebSocketFrame, IO, Stream]] =
    ZLayer:
      for
        client <- ZIO.service[Client]
      yield
        ZIOShowdownClient(client, serverUrl)

  def openConnection(handler: ShowdownConnection[WebSocketFrame, IO, Stream] => IO[ProtocolError, Unit]): ZIO[ShowdownClient[WebSocketFrame, IO, Stream], ProtocolError, Unit] =
    ZIO.serviceWithZIO(_.openConnection(handler))

  def openConnectionEnv(handler: ZIO[ShowdownConnection[WebSocketFrame, IO, Stream], ProtocolError, Unit]): ZIO[ShowdownClient[WebSocketFrame, IO, Stream], ProtocolError, Unit] =
    openConnection(connection => handler.provide(ZLayer.succeed(connection)))