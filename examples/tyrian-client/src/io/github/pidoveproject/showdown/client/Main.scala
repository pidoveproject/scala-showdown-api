package io.github.pidoveproject.showdown.client

import cats.effect.IO
import cats.implicits.*
import io.github.pidoveproject.showdown.ShowdownData
import io.github.pidoveproject.showdown.client.ClientState.Main
import io.github.pidoveproject.showdown.protocol.client.{AuthCommand, GlobalCommand}
import io.github.pidoveproject.showdown.protocol.{CurrentUser, LoginResponse, ProtocolError}
import io.github.pidoveproject.showdown.protocol.server.GlobalMessage
import io.github.pidoveproject.showdown.protocol.server.ServerMessage
import io.github.pidoveproject.showdown.room.RoomId
import io.github.pidoveproject.showdown.user.Username
import io.github.pidoveproject.showdown.tyrian.{TyrianConnectionEvent, TyrianShowdownClient}
import tyrian.Html.*
import tyrian.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object Main extends TyrianApp[ClientMessage, ClientApp]:

  def router: Location => ClientMessage = Routing.none(ClientMessage.None)

  def init(flags: Map[String, String]): (ClientApp, Cmd[IO, ClientMessage]) =
    (
      ClientApp(
        state = ClientState.Connect,
        client = TyrianShowdownClient[IO],
        connection = None,
        showdownState = ShowdownData.empty
      ),
      Cmd.None
    )

  def update(app: ClientApp): ClientMessage => (ClientApp, Cmd[IO, ClientMessage]) =
    case ClientMessage.Combine(messages) =>
      (app, messages.map(Cmd.emit).foldLeft[Cmd[IO, ClientMessage]](Cmd.None)(_ |+| _))

    case ClientMessage.ShowdownEvent(TyrianConnectionEvent.Receive(messages)) =>
      println(messages.mkString("> ", "\n> ", ""))

      val validMessages = messages.collect:
        case Right(message: ServerMessage) => message

      validMessages.foldLeft[(ClientApp, Cmd[IO, ClientMessage])]((app, Cmd.None)):
        case ((state, cmd), message) =>
          val (newState, newCmd) = state.updateShowdown(message)
          (newState, cmd |+| newCmd)

    case ClientMessage.Open(connection) =>
      (app.connected(connection), Cmd.None)

    case ClientMessage.Connect =>
      (
        app,
        app.client.openConnection().map:
          case Right(connection) => ClientMessage.Open(connection)
          case Left(error) => ClientMessage.None
      )

    case msg =>
      val updated = app.updateState(msg)
      updated.connection.fold((updated, Cmd.None)): connection =>
        updated.updateConnected(msg, connection)

  def view(app: ClientApp): Html[ClientMessage] =
    app.state.view(app)

  def subscriptions(model: ClientApp): Sub[IO, ClientMessage] =
    model.connection.fold(Sub.None)(_.serverMessages.map(ClientMessage.ShowdownEvent.apply))
