package io.github.pidoveproject.showdown.protocol.server.tournament

import zio.json.JsonDecoder

/**
 * The state of a tournament battle
 */
enum BattleState:

  /**
   * The battle has not started yet but can be initiated.
   */
  case Available

  /**
   * The participants are challenging themselves but the battle has not started yet.
   */
  case Challenging

  /**
   * The participants are battling.
   */
  case InProgress

  /**
   * The battle is finished.
   */
  case Finished

  /**
   * The battle is not available.
   */
  case Unavailable

object BattleState:

  /**
   * Get the [[BattleState]] by name.
   *
   * @param name the name of the state to retrieve
   * @return the corresponding state
   */
  def fromName(name: String): Option[BattleState] =
    BattleState.values.find(_.toString equalsIgnoreCase name)

  given JsonDecoder[BattleState] =
    JsonDecoder
      .string
      .mapOrFail(BattleState.fromName(_).toRight("Invalid battle state"))
