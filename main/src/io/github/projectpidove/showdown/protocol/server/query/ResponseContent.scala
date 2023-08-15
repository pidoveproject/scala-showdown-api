package io.github.projectpidove.showdown.protocol.server.query

import io.github.projectpidove.showdown.protocol.MessageDecoder

enum ResponseContent derives MessageDecoder:
  case RoomList(rooms: Rooms)
  case UserDetails(details: UserInfo)
