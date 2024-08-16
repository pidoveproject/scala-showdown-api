package io.github.pidoveproject.showdown.room

import io.github.pidoveproject.showdown.FormatName
import io.github.pidoveproject.showdown.user.User

/**
 * A chat message.
 */
enum ChatMessage:

  /**
   * A message sent by the server.
   *
   * @param content the content of the message
   */
  case Server(content: ChatContent)

  /**
   * A message sent by a user.
   *
   * @param sender the message sender
   * @param content the content of the message
   */
  case Sent(sender: User, content: ChatContent)

  /**
   * An HTML message sent by the server.
   *
   * @param content the content of the message encoded in HTML
   */
  case Html(content: HTML)

  /**
   * A named HTML message sent by the server.
   *
   * @param name the id of the message, used for updating
   * @param content the content of the message
   */
  case UHtml(name: String, content: HTML)

  /**
   * A join message.
   *
   * @param user the user who joined the room
   */
  case Join(user: User)

  /**
   * A leave message.
   *
   * @param user the user who left the room
   */
  case Leave(user: User)

  /**
   * A challenge request.
   *
   * @param opponent the opponent from the point of view of the current user
   * @param format the format of the battle
   */
  case Challenge(opponent: User, format: FormatName)
