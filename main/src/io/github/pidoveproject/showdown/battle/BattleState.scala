package io.github.pidoveproject.showdown.battle

/**
 * The state of a [[Battle]].
 */
enum BattleState:

  /**
   * The game is preparing.
   */
  case Initialization

  /**
   * Players are choosing their lead.
   */
  case Preview

  /**
   * The game started.
   */
  case Playing
