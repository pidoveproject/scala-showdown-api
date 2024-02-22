package io.github.pidoveproject.showdown.protocol.server.query

import io.github.pidoveproject.showdown.protocol.MessageDecoder
import zio.json.*

/**
 * The list of battle rooms.
 *
 * @param rooms a Map assciating a room name to its info
 */
case class BattleRooms(rooms: Map[String, BattleRoomInfo]) derives JsonDecoder

object BattleRooms:

  def from(entries: (String, BattleRoomInfo)*): BattleRooms = BattleRooms(entries.toMap)

  given MessageDecoder[BattleRooms] = MessageDecoder.fromJson
