package io.github.projectpidove.showdown.protocol.server

import io.github.projectpidove.showdown.Timestamp as RoomTimestamp
import io.github.projectpidove.showdown.protocol.{MessageDecoder, MessageName}
import io.github.projectpidove.showdown.protocol.MessageDecoder.given
import io.github.projectpidove.showdown.room.{given, *}
import RoomType.decoder
import io.github.projectpidove.showdown.user.{UserList, User, given}

enum RoomMessage:
  // Initialization
  case Init(roomType: RoomType)
  case Title(title: String)
  case Users(users: UserList)

  @MessageName("") case Message(content: String) // TODO support `MESSAGE` format
  case Html(content: HTML)
  case UHtml(name: String, content: HTML)
  case UHtmlChange(name: String, content: HTML)
  @MessageName("join", "j", "J") case Join(user: User)
  @MessageName("leave", "l", "L") case Leave(user: User)
  case Name(newName: User, oldName: User)
  @MessageName("chat", "c") case Chat(user: User, message: ChatMessage)
  case Notify(title: String, message: String, token: Option[HighlightToken])
  @MessageName(":") case Timestamp(time: RoomTimestamp)
  @MessageName("c:") case TimestampChat(time: RoomTimestamp, user: User, message: ChatMessage)
  case Battle(room: RoomId, firstUser: User, secondUser: User)

object RoomMessage:

  given MessageDecoder[RoomMessage] = MessageDecoder.derived[RoomMessage]
