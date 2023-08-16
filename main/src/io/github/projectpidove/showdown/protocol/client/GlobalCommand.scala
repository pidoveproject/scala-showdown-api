package io.github.projectpidove.showdown.protocol.client

import io.github.projectpidove.showdown.protocol.MessageEncoder
import io.github.projectpidove.showdown.FormatName
import io.github.projectpidove.showdown.room.{ChatMessage, RoomId, given}
import io.github.projectpidove.showdown.user.{Username, given}

enum GlobalCommand derives MessageEncoder:
  case Report(user: Username, reason: String)
  case Msg(user: Username, message: ChatMessage)
  case Reply(message: ChatMessage)
  case LogOut()
  case Challenge(user: Username, format: FormatName)
  case Search(format: FormatName)
  case Rating(user: Option[Username])
  case Whois(user: Option[Username])
  case User(user: Option[Username])
  case Join(room: RoomId)
  case Leave(room: Option[RoomId])
  case UserAuth(user: Username)
  case RoomAuth(room: RoomId)
