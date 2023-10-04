package io.github.projectpidove.showdown

import io.github.projectpidove.showdown.protocol.ProtocolError
import zio.*
import zio.http.*

class ZIOShowdownClient(client: Client, serverUrl: String) extends ShowdownClient[WebSocketFrame, ProtocolTask]:

  override def openConnection(handler: ShowdownConnection[WebSocketFrame, ProtocolTask] => ProtocolTask[Unit]): ProtocolTask[Unit] =

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

  def layer(serverUrl: String = "wss://sim3.psim.us/showdown/websocket"): ZLayer[Client, Nothing, ZIOShowdownClient] =
    ZLayer:
      for
        client <- ZIO.service[Client]
      yield
        ZIOShowdownClient(client, serverUrl)

  def openConnection(handler: ShowdownConnection[WebSocketFrame, ProtocolTask] => ProtocolTask[Unit]): ZIO[ShowdownClient[WebSocketFrame, ProtocolTask], ProtocolError, Unit] =
    ZIO.serviceWithZIO(_.openConnection(handler))

  def openConnectionEnv(handler: ZIO[ShowdownConnection[WebSocketFrame, ProtocolTask], ProtocolError, Unit]): ZIO[ShowdownClient[WebSocketFrame, ProtocolTask], ProtocolError, Unit] =
    openConnection(connection => handler.provide(ZLayer.succeed(connection)))