package io.github.projectpidove.showdown.protocol.client

import io.github.projectpidove.showdown.protocol.{MessageEncoder, MessageName}
import io.github.projectpidove.showdown.FormatName
import io.github.projectpidove.showdown.room.{ChatMessage, RoomId, given}
import io.github.projectpidove.showdown.user.{AvatarName, Username, given}

enum OptionCommand derives MessageEncoder:
  case Nick(user: Option[Username])
  case Avatar(user: AvatarName)
  case Ignore(user: Username)
  case Status(note: String)
  case ClearStatus()
  case Away()
  case Busy()
  case DoNotDisturb()
  case Back()
  case TimeStamps(target: TimestampTarget, timeInterval: TimeInterval)
  case ShowJoins(room: RoomId) // SAME
  case HideJoins(room: RoomId) // SAME
  case BlockChallenges()
  case UnblockChallenges()
  @MessageName("blockpms") case BlockPrivateMessages(exeptGroup: Option[PmGroup]) // SAME