package io.github.projectpidove.showdown.protocol.server

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.Timestamp as TimestampValue
import io.github.projectpidove.showdown.battle.{*, given}
import io.github.projectpidove.showdown.protocol.{MessageDecoder, messageName}
import io.github.projectpidove.showdown.room.ChatContent
import io.github.projectpidove.showdown.team.MoveName
import io.github.projectpidove.showdown.user.{User, Username}

/**
 * A message related to general battle progress.
 */
enum BattleProgressMessage derives MessageDecoder:

  /**
   * Insert a message separator.
   */
  @messageName("") case ClearMessageBar()

  /**
   * A timer-related message.
   *
   * @param message the message content
   */
  @messageName("inactive") case TimerMessage(message: ChatContent)

  /**
   * The timer has been disabled.
   *
   * @param message the content of the disabling message
   */
  @messageName("inactiveoff") case TimerDisabled(message: ChatContent)

  /**
   * Keep-alive message.
   */
  case Upkeep()

  /**
   * An update on the current turn.
   *
   * @param number the current turn
   */
  case Turn(number: TurnNumber)

  /**
   * The battle ended with a winner.
   *
   * @param user the winner of the game
   */
  case Win(user: Username)

  /**
   * The battle resulted in a tie.
   */
  case Tie()

  /**
   * An update on the battle timestamp.
   *
   * @param timestamp the current timestamp of the battle
   */
  @messageName("t:") case Timestamp(timestamp: TimestampValue)