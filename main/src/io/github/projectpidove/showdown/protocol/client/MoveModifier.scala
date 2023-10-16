package io.github.projectpidove.showdown.protocol.client

import io.github.projectpidove.showdown.protocol.{MessageDecoder, MessageEncoder}

/**
 * A modifier usable with a move.
 */
enum MoveModifier derives MessageEncoder, MessageDecoder:

  /**
   * Mega evolve.
   */
  case Mega()

  /**
   * Make the move a Z move.
   */
  case ZMove()

  /**
   * Dynamax/Gigantamax.
   */
  case Max()