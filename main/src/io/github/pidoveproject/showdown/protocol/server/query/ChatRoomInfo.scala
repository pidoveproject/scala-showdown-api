package io.github.pidoveproject.showdown.protocol.server.query

import io.github.iltotore.iron.zioJson.given
import io.github.pidoveproject.showdown.Count
import zio.json.*

/**
 * Informations on a chat room.
 * 
 * @param title the title of the room
 * @param description the description of the room
 * @param userCount the number of users in the room
 * @param section the section where the room belongs to
 * @param subRooms the children of the room if any
 * @param privacy the privacy settings of the room if any
 */
case class ChatRoomInfo(
  title: String,
  @jsonField("desc") description: String,
  userCount: Count,
  section: Option[String] = None,
  subRooms: Option[List[String]] = None,
  privacy: Option[String] = None
) derives JsonDecoder