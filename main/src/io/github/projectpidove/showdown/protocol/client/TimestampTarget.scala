package io.github.projectpidove.showdown.protocol.client

import io.github.projectpidove.showdown.protocol.{MessageEncoder, MessageName}

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
  @MessageName("pms") case PrivateMessages
