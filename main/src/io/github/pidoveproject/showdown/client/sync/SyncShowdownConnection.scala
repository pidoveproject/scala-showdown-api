package io.github.pidoveproject.showdown.client.sync

import io.github.pidoveproject.showdown.client.ShowdownConnection
import io.github.pidoveproject.showdown.client.zio.ZIOShowdownConnection
import io.github.pidoveproject.showdown.protocol.ProtocolError
import io.github.pidoveproject.showdown.protocol.client.ClientMessage
import io.github.pidoveproject.showdown.protocol.server.ServerMessage
import io.github.pidoveproject.showdown.room.RoomId
import zio.*
import zio.http.*

class SyncShowdownConnection(runtime: Runtime[ZIOShowdownConnection])
    extends ShowdownConnection[String, [e, r] =>> r, [r] =>> PartialFunction[r, Boolean] => Unit]:

  override def sendRawMessage(message: String): Unit =
    ZIOShowdownConnection.sendRawMessage(WebSocketFrame.text(message)).runThrowFailure(runtime)

  override def sendMessage(room: RoomId, message: ClientMessage): Unit =
    ZIOShowdownConnection.sendMessage(room, message).runThrowFailure(runtime)

  override def sendMessage(message: ClientMessage): Unit =
    ZIOShowdownConnection.sendMessage(message).runThrowFailure(runtime)

  override def disconnect(): Unit = ZIOShowdownConnection.disconnect().runThrowFailure(runtime)

  override def serverMessages: PartialFunction[Either[ProtocolError, ServerMessage], Boolean] => Unit = f =>
    ZIOShowdownConnection.serverMessages.runForeachWhile(msg => ZIO.succeedBlocking(f.applyOrElse(msg, _ => true))).runThrowFailure(runtime)
