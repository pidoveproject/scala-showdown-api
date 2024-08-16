package io.github.pidoveproject.showdown.room

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.collection.MaxLength
import io.github.pidoveproject.showdown.protocol.server.RoomMessage

case class RoomChat(messages: List[ChatMessage] :| MaxLength[100]):

  /**
   * Add a chat message.
   *
   * @param message the message to add
   * @return a new room chat containing the given message
   * @note if this chat already has 100 messages, the returned copy will not contain the oldest one
   */
  def withChatMessage(message: ChatMessage): RoomChat =
    val withNewMessage = messages.prepended(message)
    val size = withNewMessage.size
    val result =
      if size > 100 then withNewMessage.dropRight(size - 100)
      else withNewMessage

    this.copy(messages = result.assume)

  /**
   * Update this chat according to the passed server event/message.
   *
   * @param message the message sent by the server
   * @return a new [[RoomChat]] updated according to the given message
   */
  def update(message: RoomMessage): RoomChat = message match
    case RoomMessage.Message(content)                => this.withChatMessage(ChatMessage.Server(content))
    case RoomMessage.Chat(user, content)             => this.withChatMessage(ChatMessage.Sent(user, content))
    case RoomMessage.TimestampChat(_, user, content) => this.withChatMessage(ChatMessage.Sent(user, content))
    case RoomMessage.Html(content)                   => this.withChatMessage(ChatMessage.Html(content))
    case RoomMessage.UHtml(name, content) =>
      val result = messages.collect:
        case ChatMessage.UHtml(n, _) if n == name => ChatMessage.UHtml(name, content)
        case message                              => message

      this.copy(messages = result.assume)
    case _ => this

object RoomChat:

  /**
   * An empty room chat.
   */
  val empty: RoomChat = RoomChat(List.empty.assume)
