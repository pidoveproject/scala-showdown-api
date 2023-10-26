package io.github.projectpidove.showdown.client

import cats.effect.IO
import cats.implicits.*
import io.github.projectpidove.showdown.ShowdownData
import io.github.projectpidove.showdown.client.ClientState.Main
import io.github.projectpidove.showdown.protocol.client.{AuthCommand, GlobalCommand}
import io.github.projectpidove.showdown.protocol.{CurrentUser, LoginResponse}
import io.github.projectpidove.showdown.protocol.server.GlobalMessage
import io.github.projectpidove.showdown.room.RoomId
import io.github.projectpidove.showdown.user.Username
import io.github.projectpidove.showdown.tyrian.{TyrianConnectEvent, TyrianLoginResponse, TyrianServerEvent, TyrianShowdownClient}
import tyrian.Html.*
import tyrian.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object Main extends TyrianApp[ClientMessage, ClientApp]:

  def router: Location => ClientMessage = Routing.none(ClientMessage.None)

  def init(flags: Map[String, String]): (ClientApp, Cmd[IO, ClientMessage]) =
    (ClientApp(ClientState.Connect, None, ShowdownData.empty), Cmd.None)

  def update(app: ClientApp): ClientMessage => (ClientApp, Cmd[IO, ClientMessage]) =
    case ClientMessage.ShowdownEvent(TyrianServerEvent.Receive(messages)) =>
      println(messages.mkString("> ", "\n> ", ""))

      val validMessages = messages.collect:
        case Right(message) => message

      (validMessages.foldLeft(app)(_.updateShowdown(_)), Cmd.None)

    case ClientMessage.ShowdownEvent(TyrianConnectEvent.Open(connection)) =>
      (app.connected(connection), Cmd.None)

    case ClientMessage.Connect =>
      (app, TyrianShowdownClient.openConnection[IO]("wss://sim3.psim.us/showdown/websocket").map(ClientMessage.ShowdownEvent.apply))

    case msg =>
      val updated = app.updateState(msg)
      updated.connection.fold((updated, Cmd.None)): connection =>
        updated.updateConnected(msg, connection)

  def view(app: ClientApp): Html[ClientMessage] =
    app.state.view(app)

  def subscriptions(model: ClientApp): Sub[IO, ClientMessage] =
    model.connection.fold(Sub.None)(_.subscribe(ClientMessage.ShowdownEvent.apply))
