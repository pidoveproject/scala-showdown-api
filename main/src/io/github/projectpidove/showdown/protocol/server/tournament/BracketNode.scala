package io.github.projectpidove.showdown.protocol.server.tournament

import io.github.iltotore.iron.zioJson.given
import io.github.projectpidove.showdown.room.RoomId
import zio.json.*

enum BracketNode derives JsonDecoder:
  case Node( //TODO make invalid states impossible
      children: List[BracketNode],
      state: BattleState,
      team: Option[String],
      result: Option[BattleResult],
      score: Option[List[Int]],
      room: Option[RoomId]
  )
  case Leaf(team: String)
