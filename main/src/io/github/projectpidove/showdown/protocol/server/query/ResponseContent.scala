package io.github.projectpidove.showdown.protocol.server.query

import io.github.projectpidove.showdown.protocol.MessageDecoder

/**
 * The content of a query response
 */
enum ResponseContent derives MessageDecoder:

  /**
   * The list of battle rooms
   */
  case RoomList(rooms: Rooms)

  /**
   * The details of a user
   */
  case UserDetails(details: UserInfo)
