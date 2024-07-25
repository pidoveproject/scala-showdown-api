package io.github.pidoveproject.showdown

import io.github.iltotore.iron.*
import io.github.pidoveproject.showdown.protocol.*
import io.github.pidoveproject.showdown.protocol.client.{AuthCommand, ClientMessage}
import io.github.pidoveproject.showdown.protocol.server.{GlobalMessage, ServerMessage}
import io.github.pidoveproject.showdown.room.RoomId
import io.github.pidoveproject.showdown.user.Username
import zio.*
import zio.http.*
import zio.json.*
import zio.stream.*

class ZIOShowdownConnection(
                             client: Client,
                             channel: WebSocketChannel
) extends ShowdownConnection[WebSocketFrame, IO, Stream]:

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

  override val serverMessages: Stream[ProtocolError, Either[ProtocolError, ServerMessage]] =
    def decode(text: String): Stream[ProtocolError, Either[ProtocolError, ServerMessage]] =
      ZStream
        .fromZIO(
          text.split(raw"(\r\n|\r|\n)").toList match
            case s">$room" :: tail => RoomId.applyZIO(room).map((tail, _))
            case messages => ZIO.succeed((messages, RoomId("lobby")))
        )
        .flatMap((messages, room) =>
          ZStream
            .fromIterable(messages)
            .map(message =>
              ServerMessage
                .decoder
                .decodeZPure(MessageInput.fromInput(message, room))
                .runEither
            )
        )
    
    ZStream
      .repeatZIO(channel.receive.toProtocolZIO)
      .flatMap:
        case ChannelEvent.Read(WebSocketFrame.Text(text)) => decode(text)
        case _ => ZStream.empty

  override def login(challStr: ChallStr)(name: Username, password: String): IO[ProtocolError, LoginResponse] =
    for
      response <- Client
        .request(
          url = "https://play.pokemonshowdown.com/action.php",
          method = Method.POST,
          content = Body.fromURLEncodedForm(Form(
            FormField.simpleField("act", "login"),
            FormField.simpleField("name", name.value),
            FormField.simpleField("pass", password),
            FormField.simpleField("challstr", challStr.value)
          ))
        ).provide(ZLayer.succeed(client))
        .toProtocolZIO
      body <- response.body.asString.map(_.tail).toProtocolZIO
      data <- ZIO.fromEither(body.fromJson[LoginResponse]).mapError(msg => ProtocolError.InvalidInput(body, msg))
      _ <- sendMessage(AuthCommand.Trn(name, 0, data.assertion))
    yield data

  override def loginGuest(challStr: ChallStr)(name: Username): IO[ProtocolError, String] =
    for
      response <-
        client
          .post(
            pathSuffix = "https://play.pokemonshowdown.com/action.php",
            body = Body.fromURLEncodedForm(Form(
              FormField.simpleField("act", "getassertion"),
              FormField.simpleField("userid", name.value),
              FormField.simpleField("challstr", challStr.value)
            ))
          )
          .toProtocolZIO
      assertion <- response.body.asString.toProtocolZIO
      _ <- sendMessage(AuthCommand.Trn(name, 0, assertion))
    yield assertion

object ZIOShowdownConnection:
  
  private type ConnectionTask[+A] = ZIO[ShowdownConnection[WebSocketFrame, IO, Stream], ProtocolError, A]
  private type ConnectionStream[+A] = ZStream[ShowdownConnection[WebSocketFrame, IO, Stream], ProtocolError, A]
      
  def sendRawMessage(message: WebSocketFrame): ConnectionTask[Unit] = ZIO.serviceWithZIO(_.sendRawMessage(message))
    
  def sendMessage(room: RoomId, message: ClientMessage): ConnectionTask[Unit] =
    ZIO.serviceWithZIO(_.sendMessage(room, message))
    
  def sendMessage(message: ClientMessage): ConnectionTask[Unit] = ZIO.serviceWithZIO(_.sendMessage(message))
    
  def disconnect(): ConnectionTask[Unit] = ZIO.serviceWithZIO(_.disconnect())

  def serverMessages: ConnectionStream[Either[ProtocolError, ServerMessage]] =
    ZStream.serviceWithStream(_.serverMessages)

  def login(challStr: ChallStr)(name: Username, password: String): ConnectionTask[LoginResponse] =
    ZIO.serviceWithZIO(_.login(challStr)(name, password))

  def loginGuest(challStr: ChallStr)(name: Username): ConnectionTask[String] =
    ZIO.serviceWithZIO(_.loginGuest(challStr)(name))