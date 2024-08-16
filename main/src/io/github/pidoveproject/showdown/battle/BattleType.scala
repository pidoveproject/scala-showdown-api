package io.github.pidoveproject.showdown.battle

import io.github.pidoveproject.showdown.protocol.MessageDecoder

/**
 * The type of a [[Battle]].
 */
enum BattleType derives MessageDecoder:

  /**
   * Two sides with an active pokemon on each one.
   */
  case Singles()

  /**
   * Two sides with two active pokemon on each one.
   */
  case Doubles()

  /**
   * Two sides with three active pokemon on each one.
   */
  case Triples()

  /**
   * Like [[BattleType.Doubles]].
   */
  case Multi()

  /**
   * Four sides with an active pokemon on each one.
   */
  case FreeForAll()
