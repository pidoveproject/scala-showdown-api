package io.github.projectpidove.showdown.protocol.client

import io.github.projectpidove.showdown.protocol.MessageEncoder

/**
 * A type of group.
 */
enum GroupTarget derives MessageEncoder:

  /**
   * The global group.
   */
  case Global

  /**
   * A room.
   */
  case Room
