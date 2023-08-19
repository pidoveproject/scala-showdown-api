package io.github.projectpidove.showdown.protocol.server.query

import io.github.projectpidove.showdown.protocol.MessageDecoder
import zio.json.*

/**
 * The list of battle rooms.
 *
 * @param rooms a Map assciating a room name to its info
 */
case class Rooms(rooms: Map[String, RoomInfo]) derives JsonDecoder

object Rooms:

  def from(entries: (String, RoomInfo)*): Rooms = Rooms(entries.toMap)

  given MessageDecoder[Rooms] = MessageDecoder.fromJson
