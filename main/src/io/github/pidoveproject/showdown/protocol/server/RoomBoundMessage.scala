package io.github.pidoveproject.showdown.protocol.server

import io.github.pidoveproject.showdown.protocol.MessageDecoder
import io.github.pidoveproject.showdown.room.RoomId

/**
 * A room message with the associated room.
 *
 * @param id the id of the room the message is bound to
 * @param message the message sent from the room
 */
case class RoomBoundMessage(id: RoomId, message: RoomMessage | BattleMessage)

object RoomBoundMessage:

  given MessageDecoder[RoomBoundMessage] =
    for
      id <- MessageDecoder.currentRoom
      message <- MessageDecoder.derivedUnion[RoomMessage | BattleMessage]
    yield RoomBoundMessage(id, message)
