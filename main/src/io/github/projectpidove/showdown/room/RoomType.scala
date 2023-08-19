package io.github.projectpidove.showdown.room

import io.github.projectpidove.showdown.protocol.MessageDecoder
import io.github.projectpidove.showdown.protocol.MessageDecoder.given

/**
 * The type of a room. Either a chat room or a battle/simulator one.
 */
enum RoomType:
  case Battle
  case Chat

object RoomType:

  given decoder: MessageDecoder[RoomType] = MessageDecoder.derived[RoomType]
