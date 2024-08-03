package io.github.pidoveproject.showdown.tyrian

import cats.effect.Async
import cats.syntax.all.*
import io.github.iltotore.iron.*
import io.github.pidoveproject.showdown.protocol.{LoginResponse, MessageInput, ProtocolError}
import io.github.pidoveproject.showdown.protocol.client.ClientMessage
import io.github.pidoveproject.showdown.protocol.server.ServerMessage
import io.github.pidoveproject.showdown.room.RoomId
import io.github.pidoveproject.showdown.user.Username
import io.github.pidoveproject.showdown.{ChallStr, ShowdownConnection}
import tyrian.{Cmd, Sub}
import tyrian.http.{Body, Decoder, Header, Http, HttpError, Request}
import tyrian.websocket.{WebSocket, WebSocketEvent}
import zio.json.*

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * An open connection to a Pokemon Showdown.
 *
 * @param socket the web socket used to communicate with the server
 * @tparam F the effect type of the Tyrian app
 */
case class TyrianShowdownConnection[F[_] : Async](socket: WebSocket[F])
  extends ShowdownConnection[String, [e, r] =>> Cmd[F, UnitToNothing[r]], [r] =>> Sub[F, TyrianConnectionEvent[r]]]:

  override def sendRawMessage(message: String): Cmd[F, Nothing] =
    socket.publish(message)

  override def sendMessage(room: RoomId, message: ClientMessage): Cmd[F, Nothing] =
    ClientMessage.encoder
      .encode(message)
      .fold(
        err => Cmd.SideEffect(Async[F].raiseError(err)),
        msg => sendRawMessage(msg.mkString(s"$room|/", ",", "").replaceFirst(",", " "))
      )

  override def sendMessage(message: ClientMessage): Cmd[F, Nothing] =
    ClientMessage.encoder
      .encode(message)
      .fold(
        err => Cmd.SideEffect(Async[F].raiseError(err)),
        msg => sendRawMessage(msg.mkString(s"|/", ",", "").replaceFirst(",", " "))
      )

  override def disconnect(): Cmd[F, Nothing] = socket.disconnect

  override val serverMessages: Sub[F, TyrianConnectionEvent[Either[ProtocolError, ServerMessage]]] =
    socket.subscribe:
      case WebSocketEvent.Receive(text) =>
        text
          .split(raw"(\r\n|\r|\n)")
          .toList
          .match
            case s">$room" :: tail => RoomId.either(room).map((tail, _))
            case messages => Right((messages, RoomId("lobby")))
          .match
            case Right((messages, room)) => TyrianConnectionEvent.Receive(
              messages.map(message =>
                ServerMessage
                  .decoder
                  .decodeZPure(MessageInput.fromInput(message, room))
                  .runEither
              )
            )

            case Left(error) => TyrianConnectionEvent.Receive(List(Left(ProtocolError.InvalidInput(text, error))))

      case WebSocketEvent.Open => TyrianConnectionEvent.Open
      case WebSocketEvent.Close(code, reason) => TyrianConnectionEvent.Close(code, reason)
      case WebSocketEvent.Error(error) => TyrianConnectionEvent.Error(error)
      case WebSocketEvent.Heartbeat => TyrianConnectionEvent.Heartbeat

  override def login(challStr: ChallStr)(name: Username, password: String): Cmd[F, LoginResponse] =
    val encodedName = URLEncoder.encode(name.value, StandardCharsets.UTF_8)
    val encodedPassword = URLEncoder.encode(password, StandardCharsets.UTF_8)
    val encodedChallStr = URLEncoder.encode(challStr.value, StandardCharsets.UTF_8)

    val request = Request.post(
        url = "https://play.pokemonshowdown.com/action.php",
        body = Body.PlainText(
          contentType = "application/x-www-form-urlencoded; charset=UTF-8",
          s"act=login&name=$encodedName&pass=$encodedPassword&challstr=$encodedChallStr"
        )
      )
      .withHeaders(Header("Sec-Fetch-Site", "cross-site"))

    val decoder = Decoder(
      onResponse = _.body.substring(1).fromJson[LoginResponse].left.map(ProtocolError.Miscellaneous.apply),
      onError =
        case HttpError.BadRequest(msg) => Left(ProtocolError.AuthentificationFailed(msg))
        case HttpError.Timeout => Left(ProtocolError.Miscellaneous("timeout"))
        case HttpError.NetworkError => Left(ProtocolError.Miscellaneous("network error"))
    )

    Cmd.Run(Http.send(request, decoder).toTask.rethrow)

  override def loginGuest(challStr: ChallStr)(name: Username): Cmd[F, String] =
    val request = Request.post(
      url = "https://play.pokemonshowdown.com/action.php",
      body = Body.plainText(
        s"""act: getassertion
           |userid: $name
           |challstr: $challStr""".stripMargin
      )
    )

    val decoder = Decoder(
      onResponse = r => Right(r.body),
      onError =
        case HttpError.BadRequest(msg) => Left(ProtocolError.AuthentificationFailed(msg))
        case HttpError.Timeout => Left(ProtocolError.Miscellaneous("timeout"))
        case HttpError.NetworkError => Left(ProtocolError.Miscellaneous("network error"))
    )

    Cmd.Run(Http.send(request, decoder).toTask.rethrow)