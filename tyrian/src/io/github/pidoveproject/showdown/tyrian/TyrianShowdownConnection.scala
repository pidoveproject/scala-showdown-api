package io.github.pidoveproject.showdown.tyrian

import cats.effect.Async
import io.github.pidoveproject.showdown.protocol.{LoginResponse, MessageDecoder, MessageEncoder, ProtocolError}
import io.github.pidoveproject.showdown.protocol.client.{AuthCommand, ClientMessage}
import io.github.pidoveproject.showdown.protocol.server.ServerMessage
import io.github.pidoveproject.showdown.room.RoomId
import io.github.pidoveproject.showdown.user.Username
import io.github.pidoveproject.showdown.ChallStr
import tyrian.{Cmd, Sub}
import tyrian.http.{Body, Decoder, Header, Http, Request, RequestCredentials}
import tyrian.websocket.WebSocket

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * An open connection to a Pokemon Showdown.
 *
 * @param socket the web socket used to communicate with the server
 * @tparam F the effect type of the Tyrian app
 */
case class TyrianShowdownConnection[F[_] : Async](socket: WebSocket[F]):

  /**
   * Send a socket frame to the server.
   *
   * @param message the socket message to send
   */
  def sendRawMessage[Msg](message: String): Cmd[F, Msg] = socket.publish(message)

  /**
   * Send client-bound message to the server.
   *
   * @param room    the room to send the message to
   * @param message the message to send
   */
  def sendMessage[Msg](room: RoomId, message: ClientMessage): Cmd[F, Msg] =
    MessageEncoder.derivedUnion[ClientMessage].encode(message) match
      case Right(parts) => sendRawMessage(parts.mkString(s"$room|/", ",", "").replaceFirst(",", " "))
      case Left(error) => Cmd.None

  /**
   * Send client-bound message to the server.
   *
   * @param message the message to send
   */
  def sendMessage[Msg](message: ClientMessage): Cmd[F, Msg] =
    MessageEncoder.derivedUnion[ClientMessage].encode(message) match
      case Right(parts) => sendRawMessage(parts.mkString(s"|/", ",", "").replaceFirst(",", " "))
      case Left(error) => Cmd.None

  /**
   * Disconnect from the server.
   */
  def disconnect[Msg]: Cmd[F, Msg] = socket.disconnect

  /**
   * Subscribe to the connection to receive messages.
   *
   * @param handler the message handler
   */
  def subscribe[Msg](handler: TyrianServerEvent => Msg): Sub[F, Msg] =
    socket
      .subscribe(TyrianServerEvent.fromTyrian(_)(using MessageDecoder.derivedUnion[ServerMessage]))
      .map(handler)

  /**
   * Login to a registered account.
   *
   * @param name     the name of the account
   * @param password the password of the account
   * @return the authentification response sent by the server
   */
  def login(challStr: ChallStr, name: Username, password: String): Cmd[F, TyrianLoginResponse] =
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
      onResponse = TyrianLoginResponse.fromUserLoginResponse,
      onError = TyrianLoginResponse.fromError
    )

    Http.send(request, decoder)

  /**
   * Login as guest.
   *
   * @param name the name to take in game
   */
  def loginGuest(challStr: ChallStr, name: Username): Cmd[F, TyrianLoginResponse] =
    val request = Request.post(
      url = "https://play.pokemonshowdown.com/action.php",
      body = Body.plainText(
        s"""act: getassertion
           |userid: $name
           |challstr: $challStr""".stripMargin
      )
    )

    val decoder = Decoder(
      onResponse = TyrianLoginResponse.fromGuestLoginResponse,
      onError = TyrianLoginResponse.fromError
    )

    Http.send(request, decoder)
