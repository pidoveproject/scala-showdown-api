package io.github.projectpidove.showdown

import io.github.projectpidove.showdown.protocol.ProtocolError
import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.client3.*
import sttp.model.Uri
import sttp.ws.*
import zio.*

class ZIOShowdownClient(backend: SttpBackend[Task, ZioStreams & WebSockets], uri: Uri) extends ShowdownClient[WebSocketFrame, ProtocolTask]:

  override def openConnection(handler: ShowdownConnection[WebSocketFrame, ProtocolTask] => ProtocolTask[Unit]): ProtocolTask[Unit] =
    basicRequest
      .get(uri)
      .response(asWebSocketAlways(ZIOShowdownConnection.withHandler(backend, handler)))
      .send(backend)
      .mapError(ProtocolError.Thrown.apply)
      .unit

object ZIOShowdownClient:

  def layer(uri: Uri = uri"wss://sim3.psim.us/showdown/websocket"): ZLayer[SttpBackend[Task, ZioStreams & WebSockets], Nothing, ZIOShowdownClient] =
    ZLayer:
      for
        backend <- ZIO.service[SttpBackend[Task, ZioStreams & WebSockets]]
      yield
        ZIOShowdownClient(backend, uri)

  def openConnection(handler: ShowdownConnection[WebSocketFrame, ProtocolTask] => ProtocolTask[Unit]): ZIO[ZIOShowdownClient, ProtocolError, Unit] =
    ZIO.serviceWithZIO(_.openConnection(handler))