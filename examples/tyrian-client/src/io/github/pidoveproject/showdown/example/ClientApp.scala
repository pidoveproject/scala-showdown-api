package io.github.pidoveproject.showdown.example

import _root_.cats.effect.IO
import io.github.pidoveproject.showdown.ShowdownData
import io.github.pidoveproject.showdown.protocol.{CurrentUser, LoginResponse}
import io.github.pidoveproject.showdown.protocol.client.{AuthCommand, BattleRoomCommand, ChoiceResponse, GlobalCommand}
import io.github.pidoveproject.showdown.protocol.server.GlobalMessage
import io.github.pidoveproject.showdown.tyrian.{TyrianShowdownClient, TyrianShowdownConnection}
import io.github.pidoveproject.showdown.protocol.server.ServerMessage
import io.github.pidoveproject.showdown.room.{ChatContent, ChatMessage, RoomId}
import io.github.pidoveproject.showdown.user.{User, Username}
import tyrian.Cmd

case class ClientApp(
    state: ClientState,
    client: TyrianShowdownClient[IO],
    connection: Option[TyrianShowdownConnection[IO]],
    showdownState: ShowdownData
):

  def connected(connection: TyrianShowdownConnection[IO]): ClientApp = this.copy(
    state = ClientState.Login("", ""),
    connection = Some(connection)
  )

  def updateShowdown(message: ServerMessage): (ClientApp, Cmd[IO, ClientMessage]) =
    val updatedState = state.updateShowdown(message)
    val updatedShowdown = showdownState.update(message)

    val updatedApp = this.copy(state = updatedState, showdownState = updatedShowdown)

    message match
      case GlobalMessage.UpdateUser(_, named, _, _) if showdownState.loggedUser.forall(_.isGuest) && named =>
        (updatedApp.copy(state = ClientState.Main(None, Set.empty, "", "")), Cmd.None)

      case GlobalMessage.PrivateMessage(User(sender, _), User(receiver, _), message) =>
        showdownState.loggedUser.map(_.name) match
          case Some(name) if sender == name => (updatedApp, Cmd.emit(ClientMessage.OpenPrivateMessages(receiver)))
          case Some(name) if receiver == name => (updatedApp, Cmd.emit(ClientMessage.OpenPrivateMessages(sender)))
          case _ => (updatedApp, Cmd.None)

      case _ => (updatedApp, Cmd.None)

  def updateState(message: ClientMessage): ClientApp =
    this.copy(state = state.update(message))

  def updateConnected(message: ClientMessage, connection: TyrianShowdownConnection[IO]): (ClientApp, Cmd[IO, ClientMessage]) = message match
    case ClientMessage.Login(username, password) =>
      showdownState
        .challStr
        .fold((this, Cmd.None)): challStr =>
          (this, client.login(challStr)(username, password).map(ClientMessage.LoggingIn.apply))

    case ClientMessage.LoggingIn(LoginResponse(_, assertion, CurrentUser(_, name, _))) =>
      (this, connection.confirmLogin(name, assertion))

    case ClientMessage.JoinRoom(room) =>
      (this, connection.sendMessage(GlobalCommand.Join(room)) |+| Cmd.emit(ClientMessage.ChangeTab(TabChoice.Room(room))))

    case ClientMessage.LeaveRoom(room) =>
      (this, connection.sendMessage(GlobalCommand.Leave(Some(room))))

    case ClientMessage.SendMessage(room, message) =>
      (this, connection.sendRawMessage(s"$room|$message"))

    case ClientMessage.SendPrivateMessage(user, message) =>
      (this, connection.sendMessage(GlobalCommand.Msg(user, message)))

    case ClientMessage.ChooseAction(room, choice, requestId) =>
      (this, connection.sendMessage(room, BattleRoomCommand.Choose(ChoiceResponse(choice, requestId))))

    case ClientMessage.Forfeit(room) =>
      (this, connection.sendMessage(room, BattleRoomCommand.Forfeit))

    case _ => (this, Cmd.None)
