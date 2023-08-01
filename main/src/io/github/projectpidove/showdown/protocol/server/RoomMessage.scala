package io.github.projectpidove.showdown.protocol.server

import io.github.projectpidove.showdown.protocol.{MessageDecoder, MessageName}
import io.github.projectpidove.showdown.protocol.MessageDecoder.given
import io.github.projectpidove.showdown.room.{userListDecoder, *}
import RoomType.decoder

enum RoomMessage:
  case Init(roomType: RoomType)
  case Title(title: RoomTitle)
  case Users(users: UserList)

object RoomMessage:

  given MessageDecoder[RoomMessage] = MessageDecoder.derived[RoomMessage]