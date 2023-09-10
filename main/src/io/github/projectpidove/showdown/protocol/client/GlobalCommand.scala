package io.github.projectpidove.showdown.protocol.client

import io.github.projectpidove.showdown.protocol.MessageEncoder
import io.github.projectpidove.showdown.FormatName
import io.github.projectpidove.showdown.room.{ChatContent, RoomId, given}
import io.github.projectpidove.showdown.user.{Username, given}

/**
 * A command that can be sent in any room.
 */
enum GlobalCommand derives MessageEncoder:

  /**
   * Report a user.
   *
   * @param user the name of the user to report
   * @param reason the reason of the report
   */
  case Report(user: Username, reason: String)

  /**
   * Send a private message to another player.
   *
   * @param user the recipient of the message
   * @param message the message to send
   */
  case Msg(user: Username, message: ChatContent)

  /**
   * Reply to the last private discussion.
   *
   * @param message the message to send
   */
  case Reply(message: ChatContent)

  /**
   * Log out from the current account.
   */
  case LogOut

  /**
   * Challenge another user.
   *
   * @param user the user to challenge
   * @param format the format of the battle
   */
  case Challenge(user: Username, format: FormatName)

  /**
   * Search for a battle.
   *
   * @param format the format to search for
   */
  case Search(format: FormatName)

  /**
   * Get the rating of a user in the current battle.
   *
   * @param user the user to check or the current one if `None`
   */
  case Rating(user: Option[Username])

  /**
   * Get details about a user.
   *
   * @param user the user to check or the current one if `None`
   */
  case WhoIs(user: Option[Username])

  /**
   * Search for a user.
   *
   * @param user the user to search for or the current one if `None`
   */
  case User(user: Option[Username])

  /**
   * Join a room.
   *
   * @param room the room to join
   */
  case Join(room: RoomId)

  /**
   * Leave a room.
   *
   * @param room the room to leave of the current one if `None`
   */
  case Leave(room: Option[RoomId])

  /**
   * Get all authority visible to the user.
   *
   * @param user the user to check or the current one if `None`
   */
  case UserAuth(user: Username)

  /**
   * Get the list of staff and authority of a room.
   *
   * @param room the room to leave of the current one if `None`
   */
  case RoomAuth(room: RoomId)

  /**
   * Query miscellaneous informations to the server.
   *
   * @param request the request to make to the server
   */
  case Query(request: QueryRequest)
