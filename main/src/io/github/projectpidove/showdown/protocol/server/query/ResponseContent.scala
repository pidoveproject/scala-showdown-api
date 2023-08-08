package io.github.projectpidove.showdown.protocol.server.query

import io.github.projectpidove.showdown.protocol.MessageDecoder
import io.github.projectpidove.showdown.protocol.MessageDecoder.given

enum ResponseContent:
  case RoomList(rooms: Rooms)
  case UserDetails(details: UserInfo)

object ResponseContent:

  given MessageDecoder[ResponseContent] = MessageDecoder.derived[ResponseContent]
