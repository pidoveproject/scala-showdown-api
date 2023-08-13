package io.github.projectpidove.showdown

import io.github.projectpidove.showdown.protocol.ProtocolError
import io.github.projectpidove.showdown.protocol.server.ServerMessage
import sttp.ws.WebSocket
import zio.*

class STTPShowdownClient(socket: WebSocket[Task]) extends ShowdownClient:

  override def getRawMessage: Task[String] = socket.receiveText()

  override def sendRawMessage(text: String): Task[Unit] = socket.sendText(text)

object STTPShowdownClient:

  val layer: ZLayer[WebSocket[Task], Nothing, STTPShowdownClient] = ZLayer:
    for
      socket <- ZIO.service[WebSocket[Task]]
    yield STTPShowdownClient(socket)
