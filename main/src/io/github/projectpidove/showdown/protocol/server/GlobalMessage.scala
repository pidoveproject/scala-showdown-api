package io.github.projectpidove.showdown.protocol.server

import io.github.projectpidove.showdown.{ChallStr as ChallStrContent, Count, Format, FormatCategory, GameSearch, given}
import io.github.projectpidove.showdown.protocol.{MessageDecoder, messageName}
import io.github.projectpidove.showdown.protocol.server.query.ResponseContent
import io.github.projectpidove.showdown.room.{given, *}
import io.github.projectpidove.showdown.user.{AvatarName, UserSettings, User}

/**
 * A message that can be sent from the server anywhere.
 */
enum GlobalMessage derives MessageDecoder:

  /**
   * A popup message. Used in scenarios like team validation.
   *
   * @param msg the message displayed by this popup
   */
  case Popup(msg: PopupMessage)

  /**
   * A private message (PM) sent by either the server or another player.
   *
   * @param sender the user who sent this PM
   * @param receiver the user who received this PM
   * @param message the content of this PM
   */
  @messageName("pm") case PrivateMessage(sender: User, receiver: User, message: ChatContent)

  /**
   * An update of the number of users on the server
   *
   * @param count the number of users on the server
   */
  case UserCount(count: Count)

  /**
   * The current user's name cannot be changed to `name`.
   *
   * @param name the name the current user tried to take
   * @param reason the reason of the failure
   */
  case NameTaken(name: String, reason: String)

  /**
   * The server sent a token used to login.
   *
   * @param content the challstr (token)
   */
  case ChallStr(content: ChallStrContent)

  /**
   * An update about a user. Usually the one currently logged in.
   *
   * @param user the updated user
   * @param named whether this user has a name or is guest
   * @param avatar the name of the avatar of this user
   * @param settings various settings of the user
   */
  case UpdateUser(user: User, named: Boolean, avatar: AvatarName, settings: UserSettings)

  /**
   * The list of all formats and categories
   *
   * @param categories a list of format category, containing formats
   */
  case Formats(categories: List[FormatCategory])

  /**
   * An update about the matchmaking.
   *
   * @param search the current state of game searching
   */
  case UpdateSearch(search: GameSearch)

  /**
   * The response of a previously sent query.
   *
   * @param content the content of the query response
   */
  case QueryResponse(content: ResponseContent)
