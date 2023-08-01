package io.github.projectpidove.showdown.protocol.server

import io.github.projectpidove.showdown.protocol.{MessageDecoder, MessageName}
import io.github.projectpidove.showdown.protocol.MessageDecoder.given
import io.github.projectpidove.showdown.room.{userListDecoder, Timestamp => RoomTimestamp, *}
import RoomType.decoder
import io.github.projectpidove.showdown.user.Username

enum RoomMessage:
  //Initialization
  case Init(roomType: RoomType)
  case Title(title: String)
  case Users(users: UserList)

  @MessageName("") case Message(content: String) //TODO support `MESSAGE` format
  case Html(content: HTML)
  case UHtml(name: String, content: HTML)
  case UHtmlChange(name: String, content: HTML)
  case Join(user: Username)
  case Leave(user: Username)
  case Name(newName: Username, oldName: Username)
  case Chat(user: Username, message: String)
  @MessageName("notify") case NotifyHighlight(title: String, message: String, token: HighlightToken) //TODO merge with notify
  case Notify(title: String, message: String)
  @MessageName(":") case Timestamp(time: RoomTimestamp)
  @MessageName("c:") case TimestampChat(time: RoomTimestamp, user: Username, message: String)
  case Battle(room: RoomId, firstUser: Username, secondUser: Username)


object RoomMessage:

  given MessageDecoder[RoomMessage] = MessageDecoder.derived[RoomMessage]