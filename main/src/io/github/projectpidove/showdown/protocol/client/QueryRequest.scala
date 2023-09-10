package io.github.projectpidove.showdown.protocol.client

import io.github.projectpidove.showdown.protocol.{MessageEncoder, MessageName}
import io.github.projectpidove.showdown.user.Username

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
  @MessageName("roomlist") case BattleRooms()

  /**
   * Get a list of available chat rooms.
   */
  @MessageName("rooms") case ChatRooms()