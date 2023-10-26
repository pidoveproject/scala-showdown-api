package io.github.projectpidove.showdown.client

import cats.effect.IO
import io.github.projectpidove.showdown.ShowdownData
import io.github.projectpidove.showdown.protocol.{CurrentUser, LoginResponse}
import io.github.projectpidove.showdown.protocol.client.{AuthCommand, GlobalCommand}
import io.github.projectpidove.showdown.protocol.server.GlobalMessage
import io.github.projectpidove.showdown.tyrian.{TyrianLoginResponse, TyrianShowdownConnection}
import io.github.projectpidove.showdown.protocol.server.ServerMessage
import io.github.projectpidove.showdown.room.{ChatContent, ChatMessage, RoomId}
import io.github.projectpidove.showdown.user.Username
import tyrian.Cmd

case class ClientApp(
    state: ClientState,
    connection: Option[TyrianShowdownConnection[IO]],
    showdownState: ShowdownData
):

  def connected(connection: TyrianShowdownConnection[IO]): ClientApp = this.copy(
    state = ClientState.Login("", ""),
    connection = Some(connection)
  )

  def updateShowdown(message: ServerMessage): ClientApp =
    val updatedState = state.updateShowdown(message)
    val updatedShowdown = showdownState.update(message)

    message match
      case GlobalMessage.UpdateUser(_, named, _, _) if showdownState.loggedUser.forall(_.isGuest) && named =>
        this.copy(state = ClientState.Main(None, "", ""), showdownState = updatedShowdown)

      case _ => this.copy(state = updatedState, showdownState = updatedShowdown)

  def updateState(message: ClientMessage): ClientApp =
    this.copy(state = state.update(message))

  def updateConnected(message: ClientMessage, connection: TyrianShowdownConnection[IO]): (ClientApp, Cmd[IO, ClientMessage]) = message match
    case ClientMessage.Login(username, password) =>
      showdownState
        .challStr
        .zip(Username.option(username))
        .fold((this, Cmd.None)): (challStr, name) =>
          (this, connection.login(challStr, name, password).map(ClientMessage.LoggingIn.apply))

    case ClientMessage.LoggingIn(TyrianLoginResponse.LogUser(LoginResponse(_, assertion, CurrentUser(_, name, _)))) =>
      (this, connection.sendMessage(AuthCommand.Trn(Username.assume(name), 0, assertion)))

    case ClientMessage.JoinRoom(room) =>
      val cmd = RoomId.option(room).fold(Cmd.None): roomId =>
        connection.sendMessage(GlobalCommand.Join(roomId)) |+| Cmd.emit(ClientMessage.ChangeTab(TabChoice.Room(roomId)))

      (this, cmd)

    case ClientMessage.LeaveRoom(room) =>
      (this, connection.sendMessage(GlobalCommand.Leave(Some(room))))

    case ClientMessage.SendMessage(room, message) =>
      (this, connection.sendRawMessage(s"$room|$message"))

    case _ => (this, Cmd.None)
