package io.github.pidoveproject.showdown.client

import io.github.pidoveproject.showdown.ShowdownData
import io.github.pidoveproject.showdown.protocol.server.{GlobalMessage, RoomBoundMessage, RoomMessage, ServerMessage}
import io.github.pidoveproject.showdown.room.{ChatContent, RoomChat, RoomId}
import io.github.pidoveproject.showdown.user.{User, Username}
import tyrian.Html
import tyrian.Html.*

enum ClientState:
  case Connect
  case Login(username: String, password: String)
  case Main(choice: Option[TabChoice], openPMs: Set[Username], choiceInput: String, messageInput: String)

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
        button(onClick(Username.option(username).fold(ClientMessage.None)(ClientMessage.Login(_, password))))("Login")
      )

    case main@Main(choice, openPMs, choiceInput, messageInput) =>
      val userInfo = app.showdownState.loggedUser match
        case Some(user) =>
          div(id := "userInfo")(
            label(s"Username: ${user.name}"),
            label(s"Avatar: ${user.avatar}"),
            label(s"Language: ${user.settings.language.getOrElse("None")}"),
          )

        case None => div(
          label("Disconnected")
        )

      val roomTabs = app.showdownState.joinedRooms.map: (id, room) =>
        val tabClass =
          choice match
            case Some(TabChoice.Room(i)) if i == id => "tab opened"
            case _ => "tab"

        div(`class` := tabClass)(
          div(`class` := "tabContent", onClick(ClientMessage.JoinRoom(id)))(
            label(`class` := "tabTitle")(room.title.getOrElse("No title")),
            label(`class` := "tabId")(id.value)
          ),
          button(`class` := "closeTab", onClick(ClientMessage.LeaveRoom(id)))("X")
        )

      val pmTabs = openPMs.map: user =>
        val tabClass =
          choice match
            case Some(TabChoice.PrivateMessage(name)) if name == user => "tab opened"
            case _ => "tab"

        div(`class` := tabClass)(
          div(`class` := "tabContent", onClick(ClientMessage.ChangeTab(TabChoice.PrivateMessage(user))))(
            label(s"Discussion with $user"),
          ),
          button(onClick(ClientMessage.ClosePrivateMessages(user)))("X")
        )

      val openedTabs = div(id := "tabs")(roomTabs.toList ++ pmTabs.toList)

      val displayedTab = choice.fold(div())(_.view(app, main))
          
      val roomChoice =
        div(id := "roomChoice")(
          label("Room/PMs choice:"),
          input(`type` := "text", name := "room_pm_choice", onInput(ClientMessage.UpdateRoomChoice.apply)),
          button(onClick(RoomId.option(choiceInput).fold(ClientMessage.None)(ClientMessage.JoinRoom.apply)))("Join"),
          button(onClick(Username.option(choiceInput).fold(ClientMessage.None)(user =>
            ClientMessage.OpenPrivateMessages(user) |+| ClientMessage.ChangeTab(TabChoice.PrivateMessage(user))
          )))("Open PMs")
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

    case Main(choice, openPMs, choiceInput, messageInput) =>
      message match
        case ClientMessage.UpdateRoomChoice(value) => Main(choice, openPMs, value, messageInput)
        case ClientMessage.UpdateChatInput(value) => Main(choice, openPMs, choiceInput, value)
        case ClientMessage.ChangeTab(value) => Main(Some(value), openPMs, choiceInput, messageInput)
        case ClientMessage.OpenPrivateMessages(user) => Main(choice, openPMs + user, choiceInput, messageInput)
        case ClientMessage.ClosePrivateMessages(user) => Main(choice, openPMs - user, choiceInput, messageInput)
        case _ => this

  def updateShowdown(message: ServerMessage): ClientState =
    this match
      case Connect => this
      case Login(username, password) => this
      case Main(Some(TabChoice.Room(currentRoom)), openPMs, choiceInput, messageInput) =>
        message match
          case RoomBoundMessage(room, RoomMessage.DeInit()) if room == currentRoom => Main(None, openPMs, choiceInput, messageInput)
          case _ => this
      case Main(choice, openPMs, choiceInput, messageInput) => this