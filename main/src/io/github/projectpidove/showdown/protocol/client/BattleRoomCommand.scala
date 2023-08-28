package io.github.projectpidove.showdown.protocol.client

import io.github.projectpidove.showdown.protocol.MessageEncoder
import io.github.projectpidove.showdown.FormatName
import io.github.projectpidove.showdown.room.{ChatMessage, RoomId, given}
import io.github.projectpidove.showdown.user.{Username, given}

enum BattleRoomCommand derives MessageEncoder:
  case SaveReplay
  case SecretRoom
  case HideRoom(isOn: Boolean)
  case PublicRoom
  case InviteOnly(isOn: Boolean)
  case InviteOnlyNext(isOn: Boolean)
  case Invite(user: Option[Username], roomName: Option[RoomId])
  case Timer(isOn: Boolean)
  case Forfeit
