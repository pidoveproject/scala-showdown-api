package io.github.projectpidove.showdown.protocol.client

import io.github.projectpidove.showdown.protocol.MessageEncoder

/**
 * A modifier usable with a move.
 */
enum MoveModifier derives MessageEncoder:

  /**
   * Mega evolve.
   */
  case Mega

  /**
   * Make the move a Z move.
   */
  case ZMove

  /**
   * Dynamax/Gigantamax.
   */
  case Max