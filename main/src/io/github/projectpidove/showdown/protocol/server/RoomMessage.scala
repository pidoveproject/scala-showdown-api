package io.github.projectpidove.showdown.protocol.server

import io.github.projectpidove.showdown.protocol.{MessageDecoder, MessageName}
import io.github.projectpidove.showdown.protocol.MessageDecoder.given
import io.github.projectpidove.showdown.room.{Timestamp => RoomTimestamp, given, *}
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
  @MessageName("join", "j", "J") case Join(user: Username)
  @MessageName("leave", "l", "L") case Leave(user: Username)
  case Name(newName: Username, oldName: Username)
  @MessageName("chat", "c") case Chat(user: Username, message: ChatMessage)
  case Notify(title: String, message: String, token: Option[HighlightToken])
  @MessageName(":") case Timestamp(time: RoomTimestamp)
  @MessageName("c:") case TimestampChat(time: RoomTimestamp, user: Username, message: ChatMessage)
  case Battle(room: RoomId, firstUser: Username, secondUser: Username)


object RoomMessage:

  given MessageDecoder[RoomMessage] = MessageDecoder.derived[RoomMessage]