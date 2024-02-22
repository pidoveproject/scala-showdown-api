package io.github.pidoveproject.showdown.protocol.client

import io.github.pidoveproject.showdown.protocol.{MessageEncoder, messageName}

/**
 * Target for timestamp-related commands.
 */
enum TimestampTarget derives MessageEncoder:

  /**
   * Every rooms.
   */
  case All

  /**
   * Only the lobby room.
   */
  case Lobby

  /**
   * Private messages
   */
  @messageName("pms") case PrivateMessages
