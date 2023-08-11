package io.github.projectpidove.showdown.protocol.server.tournament

import io.github.iltotore.iron.zioJson.given
import io.github.projectpidove.showdown.room.RoomId
import zio.json.*

enum BracketNode:
  case Node( // TODO make invalid states impossible
      children: List[BracketNode],
      state: BattleState,
      team: Option[String] = None,
      result: Option[BattleResult] = None,
      score: Option[List[Int]] = None,
      room: Option[RoomId] = None
  )
  case Leaf(team: String)

object BracketNode:

  private val nodeDecoder = DeriveJsonDecoder.gen[Node]
  private val leafDecoder = DeriveJsonDecoder.gen[Leaf]

  given JsonDecoder[BracketNode] = nodeDecoder.widen[BracketNode] <> leafDecoder.widen[BracketNode]
