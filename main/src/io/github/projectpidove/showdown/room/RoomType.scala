package io.github.projectpidove.showdown.room

import io.github.projectpidove.showdown.protocol.MessageDecoder
import io.github.projectpidove.showdown.protocol.MessageDecoder.given

enum RoomType:
  case Battle
  case Chat

object RoomType:

  given decoder: MessageDecoder[RoomType] = MessageDecoder.derived[RoomType]