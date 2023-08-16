package io.github.projectpidove.showdown

import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.client3.*
import sttp.client3.httpclient.zio.HttpClientZioBackend
import sttp.ws.WebSocket
import zio.*
import zio.json.*

import java.io.IOException

object Main extends ZIOAppDefault:

  @jsonMemberNames(CustomCase(_.toLowerCase))
  case class CurrentUser(loggedIn: Boolean, username: String, userId: String) derives JsonDecoder

  case class LoginResponse(
      @jsonField("actionsuccess") actionSuccess: Boolean,
      assertion: String,
      @jsonField("curuser") currentUser: CurrentUser
  ) derives JsonDecoder

  val challstr =
    "7e6b99c94080fde9c7236aa1792ce7fd2d9ee3e445edd21e010333020dc945e9ebbe6a9a98e3d8db7567175bc7dbf175630ea1a51f6ac1e54a1e56b887531c78538666da93b254368e4b8ce501c6e0b7537f211b00ca5b3b5cdfebd035f0dbb95f944c21c854626944ce84fd6d31426f1114580768a116616b90fa405d88d651,totoooore,1,1692007522,sim3.psim.us,094e338c7253e0721692007522,642b13ff582de22d,e5142ebc40847891;20b3784e46b811b8a3eb1712083cd3338af25e142ca5d69e101ee6ceab4db8b2f4864a1d3c31fc1687e527e9abdebbd0aa5d2198e6d383ac660f1aff7d9bd1caac2aaf31961c593d521b8a736d9b1d68a4b905ba0fc169fc322801bc35dfa24a3c8af687b1aeafd58566b82151bf12a8fa36719b69e382a8d2391256c69eadd3d86e0efc6ef2bb55a0e06793f098348a64c9ff95baf62ad0ee786e9f5f7c2d06daf21ccab683a445739a9a666dfc6fa1b090f80f1dac52becf9101b337580079267d789ba664e732c8b78b417018c5ac9df60245ace487408377a0ac1a9e8d0b9128d942f2728b1780eeb1f1e5f48f92132e2ef191819b18290e9a95f1c320e99793ff521c2be69b74867a94946ebdb167f19ad79e9ac252c356ff846502eb0a36449ad23a55f77b8029799152198623ab902263dd84d8819ff86cee9cecaa4a3bf5947276e44540bf287a854fee13e2278ee4cfc637ed6f8c97719852b02abb63b56f84ac276c43227a2928b5691cccc316e7b53ab78e05ac2c76a20c5acd9d7adc498c03cf158d9026ea0a0e75f93f625bddb0b6d8cdc97f5bbc25695850f5c8338ff17fe3470aaa1d3e9a82cbbd81bf232b0d71ab2530b451ab82e5da351ad245589aadd57b6498f028b08f9fa9423dfe2b6e190f35ecc122e8094b6c832b344847067765a6e7447940183e9eae42252e73ee6205f213b69f6d075341d90f"

  val receiveProgram: ZIO[ShowdownClient, Throwable, Unit] =
    for
      msg <- ShowdownClient.getMessage.map(_.toString) <> ShowdownClient.getRawMessage.map(msg => s"Err: $msg")
      _ <- Console.printLine(s"> $msg")
      _ <- msg match
        case s"|challstr|$data" =>
          for
            token <- login(data)
            command = s"|/trn Il_totore,0,$token"
            _ <- Console.printLine(command)
            _ <- ShowdownClient.sendRawMessage(command)
          yield ()

        case _ => ZIO.unit
    yield ()

  val commandProgram: ZIO[ShowdownClient, Throwable, String] =
    for
      line <- Console.readLine
      _ <- Console.printLine(s"< $line")
      _ <- ZIO.unless(line == "stop")(ShowdownClient.sendRawMessage(line))
    yield line

  val socketProgram: ZIO[WebSocket[Task], Throwable, Unit] =
    clientProgram
      .provideLayer(STTPShowdownClient.layer)

  def clientProgram: ZIO[ShowdownClient, Throwable, Unit] =
    ZIO.scoped:
      for
        _ <- receiveProgram.forever.forkScoped
        _ <- commandProgram.repeatWhile(_ != "stop")
      yield ()

  def login(challstr: String): Task[String] =
    for
      _ <- Console.printLine("Logging in")
      backend <- HttpClientZioBackend()
      response <-
        basicRequest
          .post(uri"https://play.pokemonshowdown.com/action.php")
          .body(
            "act" -> "login",
            "name" -> "Il_totore",
            "pass" -> "gamegie95740",
            "challstr" -> challstr
          )
          .send(backend)
      body = response.body.merge.tail
      _ <- Console.printLine(s"Response = $body")
      data <- ZIO.fromEither(body.fromJson[LoginResponse]).mapError(new IOException(_))
    yield data.assertion

  def useWebSocket(ws: WebSocket[Task]): Task[Unit] =
    socketProgram.provideLayer(ZLayer.succeed(ws))

  // create a description of a program, which requires SttpClient dependency in the environment
  def connectToShowdown(backend: SttpBackend[Task, ZioStreams & WebSockets]): Task[Response[Unit]] =
    basicRequest.get(uri"wss://sim3.psim.us/showdown/websocket").response(asWebSocketAlways(useWebSocket)).send(backend)

  override def run =
    HttpClientZioBackend.scoped().flatMap(connectToShowdown)
