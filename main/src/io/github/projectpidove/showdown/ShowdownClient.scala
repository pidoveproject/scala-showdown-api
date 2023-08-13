package io.github.projectpidove.showdown

import io.github.projectpidove.showdown.protocol.ProtocolError
import io.github.projectpidove.showdown.protocol.client.ClientMessage
import io.github.projectpidove.showdown.protocol.server.ServerMessage
import io.github.projectpidove.showdown.protocol.MessageDecoder
import io.github.projectpidove.showdown.protocol.MessageEncoder
import MessageDecoder.{given, *}
import zio.*

trait ShowdownClient:

  def getRawMessage: Task[String]

  def sendRawMessage(text: String): Task[Unit]

  def getMessage: IO[ProtocolError, ServerMessage] =
    for
      raw <- getRawMessage.mapError(ProtocolError.Thrown.apply)
      msg <- ZIO.fromEither(raw.decode[ServerMessage])
    yield
      msg

  def sendMessage(message: ClientMessage): IO[ProtocolError, Unit] =
    for
      encoded <- ZIO.fromEither(summon[MessageEncoder[ClientMessage]].encode(message))
      raw = "|" + encoded.mkString(" ")
      _ <- sendRawMessage(raw).mapError(ProtocolError.Thrown.apply)
    yield
      ()


object ShowdownClient:

  private type ClientIO[+E, +A] = ZIO[ShowdownClient, E, A]

  def getRawMessage: ClientIO[Throwable, String] =
    ZIO.serviceWithZIO(_.getRawMessage)
    
  def sendRawMessage(text: String): ClientIO[Throwable, Unit] =
    ZIO.serviceWithZIO(_.sendRawMessage(text))

  def getMessage: ClientIO[ProtocolError, ServerMessage] =
    ZIO.serviceWithZIO(_.getMessage)

  def sendMessage(message: ClientMessage): ClientIO[ProtocolError, Unit] =
    ZIO.serviceWithZIO(_.sendMessage(message))