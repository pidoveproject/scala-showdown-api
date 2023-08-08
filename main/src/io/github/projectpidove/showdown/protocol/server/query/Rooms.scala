package io.github.projectpidove.showdown.protocol.server.query

import io.github.projectpidove.showdown.protocol.MessageDecoder
import zio.json.*

case class Rooms(rooms: Map[String, RoomInfo]) derives JsonDecoder

object Rooms:

  def from(entries: (String, RoomInfo)*): Rooms = Rooms(entries.toMap)

  given MessageDecoder[Rooms] = MessageDecoder.fromJson
