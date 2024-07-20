package io.github.pidoveproject.showdown

import io.github.pidoveproject.showdown.protocol.ProtocolError
import zio.*
import zio.http.*
import zio.stream.Stream

class ZIOShowdownClient(client: Client, serverUrl: String) extends ShowdownClient[WebSocketFrame, ProtocolTask, Stream]:

  override def openConnection(handler: ShowdownConnection[WebSocketFrame, ProtocolTask, Stream] => ProtocolTask[Unit]): ProtocolTask[Unit] =

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

  def layer(serverUrl: String = "wss://sim3.psim.us/showdown/websocket"): ZLayer[Client, Nothing, ShowdownClient[WebSocketFrame, ProtocolTask, Stream]] =
    ZLayer:
      for
        client <- ZIO.service[Client]
      yield
        ZIOShowdownClient(client, serverUrl)

  def openConnection(handler: ShowdownConnection[WebSocketFrame, ProtocolTask, Stream] => ProtocolTask[Unit]): ZIO[ShowdownClient[WebSocketFrame, ProtocolTask, Stream], ProtocolError, Unit] =
    ZIO.serviceWithZIO(_.openConnection(handler))

  def openConnectionEnv(handler: ZIO[ShowdownConnection[WebSocketFrame, ProtocolTask, Stream], ProtocolError, Unit]): ZIO[ShowdownClient[WebSocketFrame, ProtocolTask, Stream], ProtocolError, Unit] =
    openConnection(connection => handler.provide(ZLayer.succeed(connection)))