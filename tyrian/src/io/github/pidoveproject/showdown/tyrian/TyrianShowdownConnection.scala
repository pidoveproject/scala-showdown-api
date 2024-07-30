package io.github.pidoveproject.showdown.tyrian

import cats.effect.Async
import io.github.iltotore.iron.*
import io.github.pidoveproject.showdown.protocol.{LoginResponse, MessageDecoder, MessageEncoder, MessageInput, ProtocolError}
import io.github.pidoveproject.showdown.protocol.client.{AuthCommand, ClientMessage}
import io.github.pidoveproject.showdown.protocol.server.ServerMessage
import io.github.pidoveproject.showdown.room.RoomId
import io.github.pidoveproject.showdown.user.Username
import io.github.pidoveproject.showdown.{ChallStr, ShowdownConnection}
import tyrian.{Cmd, Sub}
import tyrian.http.{Body, Decoder, Header, Http, HttpError, Request, RequestCredentials}
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
  extends ShowdownConnection[String, [e, r] =>> Cmd[F, Either[e, r]], [e, r] =>> Sub[F, TyrianConnectionEvent[e, r]]]:

  override def sendRawMessage(message: String): Cmd[F, Either[ProtocolError, Unit]] = socket.publish(message)

  override def sendMessage(room: RoomId, message: ClientMessage): Cmd[F, Either[ProtocolError, Unit]] =
    MessageEncoder.derivedUnion[ClientMessage].encode(message) match
      case Right(msg)  => sendRawMessage(msg.mkString(s"$room|/", ",", "").replaceFirst(",", " "))
      case Left(error) => Cmd.emit(Left(error))

  override def sendMessage(message: ClientMessage): Cmd[F, Either[ProtocolError, Unit]] =
    MessageEncoder.derivedUnion[ClientMessage].encode(message) match
      case Right(msg)  => sendRawMessage(msg.mkString(s"|/", ",", "").replaceFirst(",", " "))
      case Left(error) => Cmd.emit(Left(error))

  override def disconnect(): Cmd[F, Either[ProtocolError, Unit]] =
    socket.disconnect

  override def serverMessages: Sub[F, TyrianConnectionEvent[ProtocolError, Either[ProtocolError, ServerMessage]]] =
    socket.subscribe(identity).flatMap:
      case WebSocketEvent.Open => Sub.emit(TyrianConnectionEvent.Open)
      case WebSocketEvent.Receive(text) =>
        val roomDecodingResult = text.split(raw"(\r\n|\r|\n)").toList match
          case s">$room" :: tail => RoomId.either(room).map((tail, _))
          case messages => Right((messages, RoomId("lobby")))
        
        roomDecodingResult.fold(
          msg => TyrianConnectionEvent.Error(ProtocolError.InvalidInput(text, msg)),
          (messages, room) =>
            TyrianConnectionEvent.Receive(
              messages.map(message =>
                ServerMessage
                  .decoder
                  .decodeZPure(MessageInput.fromInput(message, room))
                  .runEither
              )
            )
        )

      case WebSocketEvent.Error(error) => TyrianConnectionEvent.Error(ProtocolError.Miscellaneous(error))
      case WebSocketEvent.Close(code, reason) => TyrianConnectionEvent.Close(code, reason)
      case WebSocketEvent.Heartbeat => TyrianConnectionEvent.Heartbeat

  override def login(challStr: ChallStr)(name: Username, password: String): Cmd[F, Either[ProtocolError, LoginResponse]] =
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
      onResponse = response =>
        val body = response.body.tail
        body.tail.fromJson[LoginResponse].left.map(msg => ProtocolError.InvalidInput(body, msg)),

      onError =
        case HttpError.BadRequest(msg) => Left(ProtocolError.AuthentificationFailed(msg))
        case HttpError.Timeout => Left(ProtocolError.Miscellaneous("timeout"))
        case HttpError.NetworkError => Left(ProtocolError.Miscellaneous("network error"))
    )

    Http.send(request, decoder)

  override def loginGuest(challStr: ChallStr)(name: Username): Cmd[F, Either[ProtocolError, String]] =
    val request = Request.post(
      url = "https://play.pokemonshowdown.com/action.php",
      body = Body.plainText(
        s"""act: getassertion
           |userid: $name
           |challstr: $challStr""".stripMargin
      )
    )

    val decoder = Decoder(
      onResponse = response => Right(response.body),
      onError =
        case HttpError.BadRequest(msg) => Left(ProtocolError.AuthentificationFailed(msg))
        case HttpError.Timeout => Left(ProtocolError.Miscellaneous("timeout"))
        case HttpError.NetworkError => Left(ProtocolError.Miscellaneous("network error"))
    )

    Http.send(request, decoder)
