package io.github.projectpidove.showdown

import io.github.projectpidove.showdown.protocol.{Channel, LoginResponse, MessageDecoder, MessageInput, ProtocolError}
import io.github.projectpidove.showdown.protocol.client.ClientMessage
import io.github.projectpidove.showdown.protocol.server.{GlobalMessage, ServerMessage}
import io.github.projectpidove.showdown.user.Username
import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.client3.*
import sttp.client3.httpclient.zio.HttpClientZioBackend
import sttp.model.Uri
import sttp.ws.{WebSocket, WebSocketFrame}
import zio.*
import zio.json.*

class ZIOShowdownConnection(
                             backend: SttpBackend[Task, ZioStreams & WebSockets],
                             socket: WebSocket[Task],
                             challstrRef: Ref[Option[ChallStr]]
                           ) extends ShowdownConnection[WebSocketFrame, ProtocolTask]:

  override def sendRawMessage(frame: WebSocketFrame): ProtocolTask[Unit] = socket.send(frame).mapError(ProtocolError.Thrown.apply)

  override def sendMessage(message: ClientMessage): ProtocolTask[Unit] = ???

  override def disconnect(): ProtocolTask[Unit] = sendRawMessage(WebSocketFrame.close)

  override def subscribe(handler: ServerMessage => ProtocolTask[Unit]): ProtocolTask[Unit] =
    val receive =
      for
        frame <- socket.receive().toProtocolZIO
        _ <- Console.printLine(frame.toString).toProtocolZIO
        message <- frame match
          case WebSocketFrame.Text(text, _, _) =>
            ZIO.foreach(text.split(raw"(\r\n|\r|\n)")): msg =>
              (for
                message <-
                  MessageDecoder
                    .derivedUnion[ServerMessage]
                    .decodeZPure(MessageInput.fromInput(msg))
                    .toZIO
                _ <- stateSubscription(message)
                _ <- handler(message)
              yield
                ()
              ).catchAll(err => Console.printLineError(s"Message: $text\nErr: $err")).toProtocolZIO
          case _ => ZIO.unit
      yield
        frame

    receive.repeatWhile {
      case _: WebSocketFrame.Close => false
      case _ => true
    }.unit *> Console.printLine("closed").toProtocolZIO

  override def login(name: Username, password: String): ProtocolTask[LoginResponse] =
    for
      challstr <- challstrRef.get.someOrFail(ProtocolError.Miscellaneous("A challstr is needed to login"))
      response <-
        basicRequest
          .post(uri"https://play.pokemonshowdown.com/action.php")
          .body(
            "act" -> "login",
            "name" -> name.toString,
            "pass" -> password,
            "challstr" -> challstr.toString
          )
          .send(backend)
          .toProtocolZIO
      body = response.body.merge.tail
      _ <- Console.printLine(s"Response = $body").toProtocolZIO
      data <- ZIO.fromEither(body.fromJson[LoginResponse]).mapError(msg => ProtocolError.InvalidInput(body, msg))
      _ <- sendRawMessage(WebSocketFrame.text(s"|/trn $name,0,${data.assertion}"))
    yield
      data

  override def loginGuest(name: Username): ProtocolTask[Unit] = ???

  private def stateSubscription(message: ServerMessage): ProtocolTask[Unit] = message match
    case GlobalMessage.ChallStr(content) => challstrRef.set(Some(content))
    case _ => ZIO.unit

object ZIOShowdownConnection:

  def withHandler(backend: SttpBackend[Task, ZioStreams & WebSockets], handler: ShowdownConnection[WebSocketFrame, ProtocolTask] => ProtocolTask[Unit])(socket: WebSocket[Task]): Task[ZIOShowdownConnection] =
    for
      challStrRef <- Ref.make[Option[ChallStr]](None)
      connection = ZIOShowdownConnection(backend, socket, challStrRef)
      result <- handler(connection)
    yield
      connection