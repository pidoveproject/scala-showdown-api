package io.github.projectpidove.showdown.protocol.server.tournament

import zio.json.JsonDecoder

enum BattleState:
  case Available
  case Challenging
  case InProgress
  case Finished
  case Unavailable

object BattleState:

  def fromName(name: String): Option[BattleState] =
    BattleState.values.find(_.toString equalsIgnoreCase name)

  given JsonDecoder[BattleState] =
    JsonDecoder
      .string
      .mapOrFail(BattleState.fromName(_).toRight("Invalid battle state"))