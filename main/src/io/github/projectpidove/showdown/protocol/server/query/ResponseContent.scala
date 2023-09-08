package io.github.projectpidove.showdown.protocol.server.query

import io.github.projectpidove.showdown.protocol.{MessageDecoder, MessageName}

/**
 * The content of a query response.
 */
enum ResponseContent derives MessageDecoder:

  /**
   * The list of battle rooms.
   */
  @MessageName("roomlist") case BattleRoomList(rooms: BattleRooms)

  /**
   * The list of chat rooms.
   */
  @MessageName("rooms") case ChatRoomList(rooms: ChatRooms)

  /**
   * The details of a user.
   */
  case UserDetails(details: UserInfo)
