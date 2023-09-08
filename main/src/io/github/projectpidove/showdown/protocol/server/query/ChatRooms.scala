package io.github.projectpidove.showdown.protocol.server.query

import io.github.iltotore.iron.zioJson.given
import io.github.projectpidove.showdown.Count
import io.github.projectpidove.showdown.protocol.MessageDecoder
import zio.json.*

/**
 * Informations about the available chat rooms.
 * 
 * @param rooms the list of available chat rooms
 * @param sectionTitles the room sections
 * @param userCount the number of connected users in a room
 * @param battleCount the number of currently-running battles
 */
case class ChatRooms(
  @jsonField("chat") rooms: List[ChatRoomInfo],
  sectionTitles: List[String],
  userCount: Count,
  battleCount: Count
) derives JsonDecoder

object ChatRooms:

  given MessageDecoder[ChatRooms] = MessageDecoder.fromJson