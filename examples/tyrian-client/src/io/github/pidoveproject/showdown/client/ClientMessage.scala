package io.github.pidoveproject.showdown.client

import cats.effect.IO
import io.github.pidoveproject.showdown.protocol.{LoginResponse, ProtocolError}
import io.github.pidoveproject.showdown.protocol.client.BattleChoice
import io.github.pidoveproject.showdown.protocol.server.ServerMessage
import io.github.pidoveproject.showdown.tyrian.{TyrianConnectionEvent, TyrianShowdownConnection}
import io.github.pidoveproject.showdown.room.{ChatContent, RoomId}
import io.github.pidoveproject.showdown.user.Username
import tyrian.websocket.WebSocketEvent

enum ClientMessage:
  case Open(connection: TyrianShowdownConnection[IO])
  case ShowdownEvent(event: TyrianConnectionEvent[Either[ProtocolError, ServerMessage]])
  case UpdateUsername(username: String)
  case UpdatePassword(password: String)
  case UpdateRoomChoice(room: String)
  case UpdateChatInput(message: String)
  case Connect
  case Login(username: Username, password: String)
  case LoggingIn(response: LoginResponse)
  case JoinRoom(room: RoomId)
  case LeaveRoom(room: RoomId)
  case OpenPrivateMessages(user: Username)
  case ClosePrivateMessages(user: Username)
  case ChangeTab(tab: TabChoice)
  case SendMessage(room: RoomId, message: ChatContent)
  case SendPrivateMessage(user: Username, message: ChatContent)
  case Combine(messages: List[ClientMessage])
  case ChooseAction(room: RoomId, choice: BattleChoice, requestId: Option[Int])
  case Forfeit(roomId: RoomId)
  case None
  
  def combine(other: ClientMessage): ClientMessage = this match
    case ClientMessage.Combine(messages) => ClientMessage.Combine(messages :+ other)
    case _ => ClientMessage.Combine(List(this, other))
    
  def |+|(other: ClientMessage): ClientMessage = combine(other)