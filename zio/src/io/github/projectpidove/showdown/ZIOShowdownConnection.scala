package io.github.projectpidove.showdown

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.protocol.client.{AuthCommand, ClientMessage}
import io.github.projectpidove.showdown.protocol.server.{GlobalMessage, ServerMessage}
import io.github.projectpidove.showdown.protocol.*
import io.github.projectpidove.showdown.room.RoomId
import io.github.projectpidove.showdown.user.Username
import zio.*
import zio.json.*
import zio.http.*

class ZIOShowdownConnection(
                             client: Client,
                             channel: WebSocketChannel,
                             stateRef: Ref[ShowdownData],
) extends ShowdownConnection[WebSocketFrame, ProtocolTask]:

  override def sendRawMessage(message: WebSocketFrame): ProtocolTask[Unit] =
    channel.send(ChannelEvent.Read(message)).toProtocolZIO

  override def sendMessage(room: RoomId, message: ClientMessage): ProtocolTask[Unit] =
    for
      parts <- ZIO.fromEither(MessageEncoder.derivedUnion[ClientMessage].encode(message))
      command = parts.mkString(s"$room|/", ",", "").replaceFirst(",", " ")
      _ <- sendRawMessage(WebSocketFrame.text(command))
    yield ()

  override def sendMessage(message: ClientMessage): ProtocolTask[Unit] =
    for
      parts <- ZIO.fromEither(MessageEncoder.derivedUnion[ClientMessage].encode(message))
      command = parts.mkString("|/", ",", "").replaceFirst(",", " ")
      _ <- sendRawMessage(WebSocketFrame.text(command))
    yield ()

  override def disconnect(): ProtocolTask[Unit] = sendRawMessage(WebSocketFrame.close(Status.Ok.code))

  private def readMessage(message: String, room: RoomId, text: String, handler: ServerMessage => ProtocolTask[Unit]): ProtocolTask[Unit] =
    (
      for
        msg <-
          MessageDecoder
            .derivedUnion[ServerMessage]
            .decodeZPure(MessageInput.fromInput(message, room))
            .toZIO
        _ <- stateSubscription(msg)
        _ <- handler(msg)
      yield ()
    ).catchAll(err => Console.printLineError(s"Message: $message\nErr: $err")).toProtocolZIO

  override def subscribe(handler: ServerMessage => ProtocolTask[Unit]): ProtocolTask[Unit] =
    def receive(frame: WebSocketFrame) =
      for
        message <- frame match
          case WebSocketFrame.Text(text) =>
            for
              messagesAndRoom <- text.split(raw"(\r\n|\r|\n)").toList match
                case s">$room" :: tail => RoomId.applyZIO(room).map((tail, _))
                case messages          => ZIO.succeed((messages, RoomId("lobby")))
              _ <- ZIO.foreachDiscard(messagesAndRoom._1)(readMessage(_, messagesAndRoom._2, text, handler))
            yield ()

          case _ => ZIO.unit
      yield ()


    Console.printLine("Begin subscribe").toProtocolZIO *>
    channel.receiveAll[Any] {
      case ChannelEvent.Read(frame) => receive(frame)
      case _ => ZIO.unit
    }.toProtocolZIO

  override def login(name: Username, password: String): ProtocolTask[LoginResponse] =
    for
      challstr <- stateRef.get.map(_.challStr).someOrFail(ProtocolError.Miscellaneous("A challstr is needed to login"))
      response <- Client
        .request(
          url = "https://play.pokemonshowdown.com/action.php",
          method = Method.POST,
          content = Body.fromURLEncodedForm(Form(
            FormField.simpleField("act", "login"),
            FormField.simpleField("name", name.value),
            FormField.simpleField("pass", password),
            FormField.simpleField("challstr", challstr.value)
          ))
        ).provide(ZLayer.succeed(client))
        .toProtocolZIO
      body <- response.body.asString.map(_.tail).toProtocolZIO
      data <- ZIO.fromEither(body.fromJson[LoginResponse]).mapError(msg => ProtocolError.InvalidInput(body, msg))
      _ <- sendMessage(AuthCommand.Trn(name, 0, data.assertion))
    yield data

  override def loginGuest(name: Username): ProtocolTask[String] =
    for
      challstr <- stateRef.get.map(_.challStr).someOrFail(ProtocolError.Miscellaneous("A challstr is needed to login"))
      response <-
        client
          .post(
            pathSuffix = "https://play.pokemonshowdown.com/action.php",
            body = Body.fromURLEncodedForm(Form(
              FormField.simpleField("act", "getassertion"),
              FormField.simpleField("userid", name.value),
              FormField.simpleField("challstr", challstr.value)
            ))
          )
          .toProtocolZIO
      assertion <- response.body.asString.toProtocolZIO
      _ <- sendMessage(AuthCommand.Trn(name, 0, assertion))
    yield assertion

  override def currentState: ProtocolTask[ShowdownData] = stateRef.get

  private def stateSubscription(message: ServerMessage): ProtocolTask[Unit] =
    stateRef.update(_.update(message))

object ZIOShowdownConnection:
  
  private type ConnectionTask[+A] = ZIO[ShowdownConnection[WebSocketFrame, ProtocolTask], ProtocolError, A]

  def withHandler(client: Client, aliveRef: Ref[Boolean], handler: ShowdownConnection[WebSocketFrame, ProtocolTask] => ProtocolTask[Unit])(channel: WebSocketChannel): Task[ZIOShowdownConnection] =
    for
      stateRef <- Ref.make(ShowdownData.empty)
      connection = ZIOShowdownConnection(client, channel, stateRef)
      _ <- handler(connection)
      _ <- aliveRef.set(false)
    yield
      connection
      
  def sendRawMessage(message: WebSocketFrame): ConnectionTask[Unit] = ZIO.serviceWithZIO(_.sendRawMessage(message))
    
  def sendMessage(room: RoomId, message: ClientMessage): ConnectionTask[Unit] =
    ZIO.serviceWithZIO(_.sendMessage(room, message))
    
  def sendMessage(message: ClientMessage): ConnectionTask[Unit] = ZIO.serviceWithZIO(_.sendMessage(message))
    
  def disconnect(): ConnectionTask[Unit] = ZIO.serviceWithZIO(_.disconnect())

  def subscribe(handler: ServerMessage => ProtocolTask[Unit]): ConnectionTask[Unit] =
    ZIO.serviceWithZIO(_.subscribe(handler))

  def login(name: Username, password: String): ConnectionTask[LoginResponse] =
    ZIO.serviceWithZIO(_.login(name, password))

  def loginGuest(name: Username): ConnectionTask[String] =
    ZIO.serviceWithZIO(_.loginGuest(name))
    
  def currentState: ConnectionTask[ShowdownData] = ZIO.serviceWithZIO(_.currentState)