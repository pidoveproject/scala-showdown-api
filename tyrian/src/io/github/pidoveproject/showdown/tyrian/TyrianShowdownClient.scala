package io.github.pidoveproject.showdown.tyrian

import cats.syntax.monadError.*
import cats.effect.Async
import io.github.pidoveproject.showdown.ShowdownClient
import io.github.pidoveproject.showdown.protocol.URL
import io.github.iltotore.iron.autoRefine
import tyrian.websocket.{WebSocket, WebSocketConnect}
import tyrian.{Cmd, Sub}
import io.github.pidoveproject.showdown.ChallStr
import io.github.pidoveproject.showdown.user.Username
import io.github.pidoveproject.showdown.protocol.{Assertion, LoginResponse}
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import tyrian.http.*
import zio.json.*
import io.github.pidoveproject.showdown.protocol.ProtocolError

class TyrianShowdownClient[F[_]: Async]
    extends ShowdownClient[String, [e, r] =>> Cmd[F, UnitToNothing[r]], [r] =>> Sub[F, TyrianConnectionEvent[r]], [r] =>> Cmd[F, Either[String, r]]]:

  override def openConnection(serverUrl: URL = URL("wss://sim3.psim.us/showdown/websocket")): Cmd[F, Either[String, TyrianShowdownConnection[F]]] =
    WebSocket.connect(serverUrl.value):
      case WebSocketConnect.Socket(webSocket) => Right(TyrianShowdownConnection(webSocket))
      case WebSocketConnect.Error(error)      => Left(error)

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
      onResponse = req =>
        val body = req.body.substring(1)
        body.fromJson[LoginResponse].left.map(msg => ProtocolError.InvalidInput(body, msg))
      ,
      onError =
        case HttpError.BadRequest(msg) => Left(ProtocolError.AuthentificationFailed(msg))
        case HttpError.Timeout         => Left(ProtocolError.Miscellaneous("timeout"))
        case HttpError.NetworkError    => Left(ProtocolError.Miscellaneous("network error"))
    )

    Cmd.Run(Http.send(request, decoder).toTask.rethrow)

  override def loginGuest(challStr: ChallStr)(name: Username): Cmd[F, Assertion] =
    val request = Request.post(
      url = "https://play.pokemonshowdown.com/action.php",
      body = Body.plainText(
        s"""act: getassertion
           |userid: $name
           |challstr: $challStr""".stripMargin
      )
    )

    val decoder = Decoder(
      onResponse = r => Assertion.either(r.body).left.map(msg => ProtocolError.InvalidInput(r.body, msg)),
      onError =
        case HttpError.BadRequest(msg) => Left(ProtocolError.AuthentificationFailed(msg))
        case HttpError.Timeout         => Left(ProtocolError.Miscellaneous("timeout"))
        case HttpError.NetworkError    => Left(ProtocolError.Miscellaneous("network error"))
    )

    Cmd.Run(Http.send(request, decoder).toTask.rethrow)
