package io.github.pidoveproject.showdown.protocol.server.tournament

import io.github.iltotore.iron.zioJson.given
import io.github.pidoveproject.showdown.room.RoomId
import zio.json.*

/**
 * A node in the bracket
 */
enum BracketNode:

  /**
   * A battle node of the tree.
   *
   * @param children the children of this node
   * @param state the current state of the battle represented by this node
   * @param team the looser of the node if the battle ended
   * @param result the result of the battle represented by this node
   * @param score the scores of the battle if it ended
   * @param room the battle room if the battle is in progress
   */
  case Node( // TODO make invalid states impossible
      children: List[BracketNode],
      state: BattleState,
      team: Option[String] = None,
      result: Option[BattleResult] = None,
      score: Option[List[Int]] = None,
      room: Option[RoomId] = None
  )

  /**
   * A leaf of the tree.
   *
   * @param team the team present in the tournament leaf
   */
  case Leaf(team: String)

object BracketNode:

  private val nodeDecoder = DeriveJsonDecoder.gen[Node]
  private val leafDecoder = DeriveJsonDecoder.gen[Leaf]

  given JsonDecoder[BracketNode] = nodeDecoder.widen[BracketNode] <> leafDecoder.widen[BracketNode]
