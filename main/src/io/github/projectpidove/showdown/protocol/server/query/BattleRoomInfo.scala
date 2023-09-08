package io.github.projectpidove.showdown.protocol.server.query

import io.github.projectpidove.showdown.protocol.{MessageDecoder, given}
import zio.json.*

/**
 * Informations on a battle room sent by the server.
 *
 * @param p1 the first battle participant
 * @param p2 the second battle participant
 * @param minElo the min elo of the match
 */
case class BattleRoomInfo(p1: String, p2: String, minElo: Int) derives JsonDecoder
