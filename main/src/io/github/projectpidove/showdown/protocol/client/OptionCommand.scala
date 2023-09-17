package io.github.projectpidove.showdown.protocol.client

import io.github.projectpidove.showdown.protocol.{MessageEncoder, messageName}
import io.github.projectpidove.showdown.FormatName
import io.github.projectpidove.showdown.room.{ChatContent, RoomId, given}
import io.github.projectpidove.showdown.user.{AvatarName, Username, given}

/**
 * Command related to user settings.
 */
enum OptionCommand derives MessageEncoder:

  /**
   * Change username.
   *
   * @param name the new username to take or reset if absent.
   */
  case Nick(name: Option[Username])

  /**
   * Change current user's avatar.
   *
   * @param name the new avatar to take.
   */
  case Avatar(name: AvatarName)

  /**
   * Ignore a user.
   *
   * @param user the name of the user to ignore
   */
  case Ignore(user: Username)

  /**
   * Set the current status.
   *
   * @param note the status to display to other users.
   */
  case Status(note: String)

  /**
   * Clear the status message.
   */
  case ClearStatus

  /**
   * Mark the current user as AFK.
   */
  case Away

  /**
   * Mark the current user as busy.
   */
  case Busy

  /**
   * Mark the current user as "do not disturb" and silence notifications.
   */
  case DoNotDisturb

  /**
   * Mark the current user as available.
   */
  case Back

  /**
   * Change timestamp preferences.
   *
   * @param target the type or place to change
   * @param interval the new interval to set in `target`
   */
  case Timestamps(target: TimestampTarget, interval: TimeInterval)

  /**
   * Show join messages.
   * 
   * @param room the room to enable join messages from or all if absent.
   */
  case ShowJoins(room: Option[RoomId])

  /**
   * Hide join messages.
   *
   * @param room the room to disable join messages from or all if absent.
   */
  case HideJoins(room: Option[RoomId])

  /**
   * Do not accept challenges.
   */
  case BlockChallenges

  /**
   * Accept challenges
   */
  case UnblockChallenges

  /**
   * Block private messages.
   * 
   * @param exceptGroup the groups still allowed to PM the current user
   */
  @messageName("blockpms") case BlockPrivateMessages(exceptGroup: Option[PrivateMessageGroup])
