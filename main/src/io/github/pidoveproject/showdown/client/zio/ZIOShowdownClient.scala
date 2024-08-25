package io.github.pidoveproject.showdown.client.zio

import io.github.pidoveproject.showdown.protocol.ProtocolError
import zio.*
import zio.http.*
import zio.http.URL as ZURL
import zio.json.*
import zio.stream.Stream
import io.github.pidoveproject.showdown.protocol.URL
import io.github.iltotore.iron.autoRefine
import io.github.pidoveproject.showdown.user.Username
import io.github.pidoveproject.showdown.protocol.LoginResponse
import io.github.pidoveproject.showdown.protocol.Assertion
import io.github.pidoveproject.showdown.ChallStr
import io.github.pidoveproject.showdown.client.ShowdownClient

class ZIOShowdownClient(client: Client)
    extends ShowdownClient[WebSocketFrame, IO, [r] =>> Stream[Throwable, r], [x] =>> ZIO[Scope, ProtocolError, x]]:

  override def openConnection(serverUrl: URL = URL("wss://sim3.psim.us/showdown/websocket")): ZIO[Scope, ProtocolError, ZIOShowdownConnection] =
    def socketApp(promise: Promise[ProtocolError, ZIOShowdownConnection]) =
      Handler.webSocket(channel => promise.succeed(ZIOShowdownConnection(client, channel)))

    for
      url <- ZIO.fromEither(ZURL.decode(serverUrl.value)).toProtocolZIO
      connectionPromise <- Promise.make[ProtocolError, ZIOShowdownConnection]
      _ <- client.socket(url = url, headers = Headers.empty, app = socketApp(connectionPromise)).toProtocolZIO
      connection <- connectionPromise.await
    yield connection

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
    yield data

  override def loginGuest(challStr: ChallStr)(name: Username): IO[ProtocolError, Assertion] =
    for
      response <- Client
        .request(
          url = "https://play.pokemonshowdown.com/action.php",
          method = Method.POST,
          content = Body.fromURLEncodedForm(Form(
            FormField.simpleField("act", "getassertion"),
            FormField.simpleField("userid", name.value),
            FormField.simpleField("challstr", challStr.value)
          ))
        ).provide(ZLayer.succeed(client))
        .toProtocolZIO
      assertionString <- response.body.asString.toProtocolZIO
      assertion <- Assertion.applyZIO(assertionString)
    yield assertion

object ZIOShowdownClient:

  val layer: ZLayer[Client, Nothing, ZIOShowdownClient] =
    ZLayer:
      for
        client <- ZIO.service[Client]
      yield ZIOShowdownClient(client)

  def openConnection(serverUrl: URL = URL("wss://sim3.psim.us/showdown/websocket"))
      : ZIO[ZIOShowdownClient & Scope, ProtocolError, ZIOShowdownConnection] =
    ZIO.serviceWithZIO[ZIOShowdownClient](_.openConnection(serverUrl))

  def login(challStr: ChallStr)(name: Username, password: String): ZIO[ZIOShowdownClient, ProtocolError, LoginResponse] =
    ZIO.serviceWithZIO(_.login(challStr)(name, password))

  def loginGuest(challStr: ChallStr)(name: Username): ZIO[ZIOShowdownClient, ProtocolError, Assertion] =
    ZIO.serviceWithZIO(_.loginGuest(challStr)(name))
