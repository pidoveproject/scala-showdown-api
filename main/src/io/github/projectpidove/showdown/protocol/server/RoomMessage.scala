package io.github.projectpidove.showdown.protocol.server

import io.github.projectpidove.showdown.Timestamp as RoomTimestamp
import io.github.projectpidove.showdown.protocol.{MessageDecoder, messageName}
import io.github.projectpidove.showdown.room.{given, *}
import io.github.projectpidove.showdown.user.{UserList, User, given}

/**
 * A message bound to a room
 */
enum RoomMessage derives MessageDecoder:
  // Initialization

  /**
   * Initialization message. Sent first when entering a room.
   *
   * @param roomType the type of the room. Either battle or chat.
   */
  case Init(roomType: RoomType)

  /**
   * De-initialization message. Sent when the current user leaves the lobby.
   */
  case DeInit()

  /**
   * An update on the title of the room.
   *
   * @param title the current title of the room
   */
  case Title(title: String)

  /**
   * The users in the room when joining.
   *
   * @param users the current list of users who joined the room
   */
  case Users(users: UserList)

  /**
   * A chat message sent by the server
   *
   * @param content the raw content of the message
   */
  @messageName("") case Message(content: ChatContent) // TODO support `MESSAGE` format

  /**
   * An HTML message sent by the server
   *
   * @param content the content of the message
   */
  @messageName("html", "raw") case Html(content: HTML)

  /**
   * A named HTML message sent by the server
   *
   * @param name the name of this message, used to update it later
   */
  case UHtml(name: String, content: HTML)

  /**
   * An update on a previously sent [[UHtml]] message
   *
   * @param name the name of the message to update
   * @param content the new content of the message
   */
  case UHtmlChange(name: String, content: HTML)

  /**
   * A user joined the room.
   *
   * @param user the user who joined the room
   */
  @messageName("join", "j", "J") case Join(user: User)

  /**
   * A user left the room.
   *
   * @param user the who lef the room
   */
  @messageName("leave", "l", "L") case Leave(user: User)

  /**
   * A user changed their name
   *
   * @param newName the new name of the user
   * @param oldName the old name of the user
   */
  case Name(newName: User, oldName: User)

  /**
   * A user sent a message in the room.
   *
   * @param user the user who sent the message
   * @param message the content of he message
   */
  @messageName("chat", "c") case Chat(user: User, message: ChatContent)

  /**
   * A notification was sent in the room.
   *
   * @param title the title of the notification
   * @param content the content of the notification
   * @param token the (optional) highlight token of this notification
   */
  case Notify(title: String, content: String, token: Option[HighlightToken]) // TODO make content optional

  /**
   * An update on the current time of the room.
   *
   * @param time the current timestamp of the room (UNIX format)
   */
  @messageName(":") case Timestamp(time: RoomTimestamp)

  /**
   * A chat message with a timestamp attached.
   *
   * @param time the timestamp of the message
   * @param user    the user who sent the message
   * @param message the content of he message
   */
  @messageName("c:") case TimestampChat(time: RoomTimestamp, user: User, message: ChatContent)

  /**
   * A battle started.
   *
   * @param room the id of the battle room
   * @param firstUser the first battle participant
   * @param secondUser the second battle participant
   */
  case Battle(room: RoomId, firstUser: User, secondUser: User)
