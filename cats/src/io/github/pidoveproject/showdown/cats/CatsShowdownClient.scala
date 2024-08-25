package io.github.pidoveproject.showdown.cats

import cats.syntax.all.*
import cats.effect.{Concurrent, Resource}
import fs2.Stream
import io.github.pidoveproject.showdown.client.ShowdownClient
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.client.websocket.{WSClient, WSFrame, WSRequest}
import io.github.pidoveproject.showdown.protocol.URL
import io.github.iltotore.iron.autoRefine
import io.github.pidoveproject.showdown.ChallStr
import io.github.pidoveproject.showdown.user.Username
import io.github.pidoveproject.showdown.protocol.LoginResponse
import org.http4s.Request
import org.http4s.Method
import org.http4s.UrlForm
import zio.json.*
import io.github.pidoveproject.showdown.protocol.ProtocolError
import io.github.pidoveproject.showdown.protocol.Assertion

class CatsShowdownClient[F[_]: Concurrent](httpClient: Client[F], wsClient: WSClient[F])
    extends ShowdownClient[WSFrame, [e, r] =>> F[r], [r] =>> Stream[F, r], [r] =>> Resource[F, r]]:

  override def openConnection(serverUrl: URL = URL("wss://sim3.psim.us/showdown/websocket")): Resource[F, CatsShowdownConnection[F]] =
    wsClient.connect(WSRequest(Uri.unsafeFromString(serverUrl.value))).map(CatsShowdownConnection(httpClient, _))

  override def login(challStr: ChallStr)(name: Username, password: String): F[LoginResponse] =
    httpClient.expect[String](
      Request(
        method = Method.POST,
        uri = Uri.unsafeFromString("https://play.pokemonshowdown.com/action.php")
      ).withEntity(UrlForm(
        "act" -> "login",
        "name" -> name.value,
        "pass" -> password,
        "challstr" -> challStr.value
      )).asInstanceOf[Request[F]]
    )
      .map(body => body.fromJson[LoginResponse].left.map(msg => ProtocolError.InvalidInput(body, msg)))
      .rethrow

  override def loginGuest(challStr: ChallStr)(name: Username): F[Assertion] =
    httpClient.expect[String](
      Request(
        method = Method.POST,
        uri = Uri.unsafeFromString("https://play.pokemonshowdown.com/action.php")
      ).withEntity(UrlForm(
        "act" -> "getassertion",
        "userid" -> name.value,
        "challstr" -> challStr.value
      )).asInstanceOf[Request[F]]
    ).map(input => Assertion.either(input).left.map(msg => ProtocolError.InvalidInput(input, msg))).rethrow
