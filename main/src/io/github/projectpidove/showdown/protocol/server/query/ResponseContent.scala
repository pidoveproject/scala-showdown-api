package io.github.projectpidove.showdown.protocol.server.query

import io.github.projectpidove.showdown.protocol.{MessageDecoder, messageName}

/**
 * The content of a query response.
 */
enum ResponseContent derives MessageDecoder:

  /**
   * The list of battle rooms.
   */
  @messageName("roomlist") case BattleRoomList(rooms: BattleRooms)

  /**
   * The list of chat rooms.
   */
  @messageName("rooms") case ChatRoomList(rooms: ChatRooms)

  /**
   * The details of a user.
   */
  case UserDetails(details: UserInfo)
