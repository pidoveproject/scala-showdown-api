package io.github.projectpidove.showdown.client

import cats.effect.IO
import io.github.projectpidove.showdown.tyrian.{TyrianLoginResponse, TyrianShowdownEvent}
import io.github.projectpidove.showdown.room.RoomId
import io.github.projectpidove.showdown.user.Username
import tyrian.websocket.WebSocketEvent

enum ClientMessage:
  case ShowdownEvent(event: TyrianShowdownEvent[IO])
  case UpdateUsername(username: String)
  case UpdatePassword(password: String)
  case UpdateRoomChoice(room: String)
  case UpdateChatInput(message: String)
  case Connect
  case Login(username: String, password: String)
  case LoggingIn(response: TyrianLoginResponse)
  case JoinRoom(room: String)
  case LeaveRoom(room: RoomId)
  case ChangeTab(tab: TabChoice)
  case SendMessage(room: RoomId, message: String)
  case None