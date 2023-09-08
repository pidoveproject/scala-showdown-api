package io.github.projectpidove.showdown.protocol.server

import io.github.projectpidove.showdown.protocol.MessageDecoder
import io.github.projectpidove.showdown.room.RoomId

/**
 * A room message with the associated room.
 * 
 * @param id the id of the room the message is bound to
 * @param message the message sent from the room
 */
case class RoomBoundMessage(id: RoomId, message: RoomMessage)

object RoomBoundMessage:

  given MessageDecoder[RoomBoundMessage] =
    for
      id <- MessageDecoder.currentRoom
      message <- summon[MessageDecoder[RoomMessage]]
    yield
      RoomBoundMessage(id, message)