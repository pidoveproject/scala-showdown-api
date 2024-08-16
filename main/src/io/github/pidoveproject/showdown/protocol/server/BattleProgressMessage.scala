package io.github.pidoveproject.showdown.protocol.server

import io.github.iltotore.iron.*
import io.github.pidoveproject.showdown.Timestamp as TimestampValue
import io.github.pidoveproject.showdown.battle.*
import io.github.pidoveproject.showdown.protocol.client.BattleChoice
import io.github.pidoveproject.showdown.protocol.server.choice.{ChoiceError, ChoiceRequest}
import io.github.pidoveproject.showdown.protocol.{MessageDecoder, messageName}
import io.github.pidoveproject.showdown.room.ChatContent
import io.github.pidoveproject.showdown.user.{User, Username}

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

  /**
   * The selected choice cannot be performed.
   *
   * @param error the detailed information about what went wrong
   */
  case Error(error: ChoiceError)

  /**
   * A choice request from the server.
   *
   * @param request information on available actions
   */
  case Request(request: Option[ChoiceRequest])

  /**
   * The server confirmed the choice response has been received.
   *
   * @param choice the received choice
   */
  case SentChoice(choice: BattleChoice)
