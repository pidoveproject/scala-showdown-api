package io.github.projectpidove.showdown.client

import io.github.projectpidove.showdown.protocol.server.{RoomBoundMessage, RoomMessage, ServerMessage}
import io.github.projectpidove.showdown.tyrian.TyrianLoginResponse
import tyrian.Html
import tyrian.Html.*

enum ClientState:
  case Connect
  case Login(username: String, password: String)
  case Main(choice: Option[TabChoice], choiceInput: String, messageInput: String)

  def view(app: ClientApp): Html[ClientMessage] = this match
    case Connect =>
      div()(
        label("Not connected. Click to connect."),
        button(onClick(ClientMessage.Connect))("Connect")
      )

    case Login(username, password) =>
      div()(
        label("Username:"),
        input(`type` := "text", name := "username", onInput(ClientMessage.UpdateUsername.apply)),
        label("Password:"),
        input(`type` := "password", name := "password", onInput(ClientMessage.UpdatePassword.apply)),
        button(onClick(ClientMessage.Login(username, password)))("Login")
      )

    case Main(choice, choiceInput, messsageInput) =>
      val userInfo = app.showdownState.loggedUser match
        case Some(user) =>
          div(
            label(s"Username: ${user.name}"),
            label(s"Avatar: ${user.avatar}"),
            label(s"Language: ${user.settings.language.getOrElse("None")}"),
          )

        case None => div(
          label("Disconnected")
        )

      val roomTabs = app.showdownState.joinedRooms.map: (id, room) =>
        div(
          div(onClick(ClientMessage.JoinRoom(id.value)))(
            label(room.title.getOrElse("No title")),
            label(id.value)
          ),
          button(onClick(ClientMessage.LeaveRoom(id)))("X")
        )

      val openedTabs = div(roomTabs.toList)

      val displayedTab = choice match
        case Some(TabChoice.PrivateMessage(user)) => h2(s"Discussion with $user")
        case Some(TabChoice.Room(room)) =>
          val joinedRoom = app.showdownState.getJoinedRoomOrEmpty(room)
          div(
            h2(s"${joinedRoom.title.getOrElse(joinedRoom.id)}${joinedRoom.roomType.fold("")(tpe => s" ($tpe)")}"),
            input(`type` := "text", name := "message", onInput(ClientMessage.UpdateChatInput.apply)),
            button(onClick(ClientMessage.SendMessage(room, messsageInput)))("Envoyer"),
            viewRoom(joinedRoom)
          )

        case None => div()
          
      val roomChoice =
        div(
          label("Room choice:"),
          input(`type` := "text", name := "room_choice", onInput(ClientMessage.UpdateRoomChoice.apply)),
          button(onClick(ClientMessage.JoinRoom(choiceInput)))("Join")
        )

      div(
        h1("Main menu"),
        userInfo,
        roomChoice,
        openedTabs,
        displayedTab
      )

  def update(message: ClientMessage): ClientState = this match
    case Connect => this
    case Login(username, password) =>
      message match
        case ClientMessage.UpdateUsername(value) => Login(value, password)
        case ClientMessage.UpdatePassword(value) => Login(username, value)
        case _ => this

    case Main(choice, choiceInput, messageInput) =>
      message match
        case ClientMessage.UpdateRoomChoice(value) => Main(choice, value, messageInput)
        case ClientMessage.UpdateChatInput(value) => Main(choice, choiceInput, value)
        case ClientMessage.ChangeTab(value) => Main(Some(value), choiceInput, messageInput)
        case _ => this

  def updateShowdown(message: ServerMessage): ClientState =
    this match
      case Connect => this
      case Login(username, password) => this
      case Main(Some(TabChoice.Room(currentRoom)), choiceInput, messageInput) =>
        message match
          case RoomBoundMessage(room, RoomMessage.DeInit()) if room == currentRoom => Main(None, choiceInput, messageInput)
          case _ => this
      case Main(choice, choiceInput, messageInput) => this