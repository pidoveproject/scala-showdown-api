package io.github.pidoveproject.showdown

import io.github.iltotore.iron.*
import io.github.pidoveproject.showdown.protocol.*
import io.github.pidoveproject.showdown.protocol.client.{AuthCommand, ClientMessage}
import io.github.pidoveproject.showdown.protocol.server.ServerMessage
import io.github.pidoveproject.showdown.room.RoomId
import zio.*
import zio.http.*
import zio.stream.*
import io.github.pidoveproject.showdown.user.Username

class ZIOShowdownConnection(
    client: Client,
    channel: WebSocketChannel
) extends ShowdownConnection[WebSocketFrame, IO, [r] =>> Stream[Throwable, r]]:

  override def sendRawMessage(message: WebSocketFrame): IO[ProtocolError, Unit] =
    channel.send(ChannelEvent.Read(message)).toProtocolZIO

  override def sendMessage(room: RoomId, message: ClientMessage): IO[ProtocolError, Unit] =
    for
      parts <- ZIO.fromEither(ClientMessage.encoder.encode(message))
      command = parts.mkString(s"$room|/", ",", "").replaceFirst(",", " ")
      _ <- sendRawMessage(WebSocketFrame.text(command))
    yield ()

  override def sendMessage(message: ClientMessage): IO[ProtocolError, Unit] =
    for
      parts <- ZIO.fromEither(MessageEncoder.derivedUnion[ClientMessage].encode(message))
      command = parts.mkString("|/", ",", "").replaceFirst(",", " ")
      _ <- sendRawMessage(WebSocketFrame.text(command))
    yield ()

  override def disconnect(): IO[ProtocolError, Unit] = sendRawMessage(WebSocketFrame.close(Status.Ok.code))

  override val serverMessages: Stream[Throwable, Either[ProtocolError, ServerMessage]] =
    ZStream
      .repeatZIO(channel.receive)
      .flatMap:
        case ChannelEvent.Read(WebSocketFrame.Text(text)) =>
          text
            .split(raw"(\r\n|\r|\n)")
            .toList
            .match
              case s">$room" :: tail => RoomId.either(room).map((tail, _))
              case messages          => Right((messages, RoomId("lobby")))
            .match
              case Right((messages, room)) => ZStream.fromIterable(messages).map(message =>
                  ServerMessage
                    .decoder
                    .decodeZPure(MessageInput.fromInput(message, room))
                    .runEither
                )

              case Left(error) => ZStream.from(Left(ProtocolError.InvalidInput(text, error)))
        case _ => ZStream.empty

object ZIOShowdownConnection:

  private type ConnectionTask[+A] = ZIO[ZIOShowdownConnection, ProtocolError, A]
  private type ConnectionStream[+A] = ZStream[ZIOShowdownConnection, ProtocolError, A]

  def sendRawMessage(message: WebSocketFrame): ZIO[ZIOShowdownConnection, ProtocolError, Unit] = ZIO.serviceWithZIO(_.sendRawMessage(message))

  def sendMessage(room: RoomId, message: ClientMessage): ZIO[ZIOShowdownConnection, ProtocolError, Unit] =
    ZIO.serviceWithZIO(_.sendMessage(room, message))

  def sendMessage(message: ClientMessage): ZIO[ZIOShowdownConnection, ProtocolError, Unit] = ZIO.serviceWithZIO(_.sendMessage(message))

  def confirmLogin(name: Username, assertion: Assertion): ZIO[ZIOShowdownConnection, ProtocolError, Unit] =
    ZIO.serviceWithZIO(_.confirmLogin(name, assertion))

  def disconnect(): ZIO[ZIOShowdownConnection, ProtocolError, Unit] = ZIO.serviceWithZIO(_.disconnect())

  def serverMessages: ZStream[ZIOShowdownConnection, Throwable, Either[ProtocolError, ServerMessage]] =
    ZStream.serviceWithStream(_.serverMessages)
