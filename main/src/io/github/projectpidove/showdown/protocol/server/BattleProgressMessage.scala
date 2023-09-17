package io.github.projectpidove.showdown.protocol.server

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.Timestamp as TimestampValue
import io.github.projectpidove.showdown.battle.{*, given}
import io.github.projectpidove.showdown.protocol.{MessageDecoder, messageName}
import io.github.projectpidove.showdown.room.ChatContent
import io.github.projectpidove.showdown.team.MoveName
import io.github.projectpidove.showdown.user.{User, Username}

enum BattleProgressMessage derives MessageDecoder:
  @messageName("") case ClearMessageBar()
  @messageName("inactive") case TimerMessage(message: ChatContent)
  @messageName("inactiveoff") case TimerDisabled(message: ChatContent)
  case Upkeep()
  case Turn(number: TurnNumber)
  case Win(user: Username)
  case Tie()
  @messageName("t:") case Timestamp(timestamp: TimestampValue)