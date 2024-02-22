package io.github.pidoveproject.showdown.protocol.client

import io.github.pidoveproject.showdown.protocol.{MessageEncoder, messageName}
import io.github.pidoveproject.showdown.user.Username

/**
 * A query request.
 */
enum QueryRequest derives MessageEncoder:

  /**
   * Get details of a user.
   *
   * @param name the name of the target
   */
  case UserDetails(name: Username)

  /**
   * Get a list of active battle rooms.
   */
  @messageName("roomlist") case BattleRooms()

  /**
   * Get a list of available chat rooms.
   */
  @messageName("rooms") case ChatRooms()