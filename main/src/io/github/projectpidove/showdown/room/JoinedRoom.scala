package io.github.projectpidove.showdown.room

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.Timestamp
import io.github.projectpidove.showdown.user.UserList
import io.github.iltotore.iron.constraint.collection.MaxLength
import io.github.projectpidove.showdown.protocol.server.RoomMessage
import io.github.projectpidove.showdown.user.User

/**
 * A showdown room.
 * 
 * @param id this room's id
 * @param roomType this room's type, whether chat or battle
 * @param users the users connected to this room
 * @param messages the 100 last messages of this room
 */
case class JoinedRoom(
  id: RoomId,
  title: Option[String],
  roomType: Option[RoomType],
  currentTime: Timestamp,
  users: Set[User],
  chat: RoomChat
):

  def withChatMessage(message: ChatMessage): JoinedRoom = this.copy(chat = chat.withChatMessage(message))

  /**
   * Update this room according to the passed server event/message.
   *
   * @param message the message sent by the server
   * @return a new [[JoinedRoom]] updated according to the given message
   */
  def update(message: RoomMessage): JoinedRoom = message match
    case RoomMessage.Init(roomType) => this.copy(roomType = Some(roomType))
    case RoomMessage.Title(title) => this.copy(title = Some(title))
    case RoomMessage.Users(users) => this.copy(users = users.value.toSet)
    case RoomMessage.Join(user) => this.copy(users = users + user)
    case RoomMessage.Leave(user) => this.copy(users = users - user)
    case RoomMessage.Name(newName, oldName) => this.copy(users = users - oldName + newName)
    case RoomMessage.Timestamp(time) => this.copy(currentTime = time)
    case msg: (RoomMessage.Message | RoomMessage.Html | RoomMessage.UHtml | RoomMessage.UHtmlChange | RoomMessage.TimestampChat) =>
      this.copy(chat = chat.update(msg))
    case _ => this

object JoinedRoom:

  /**
   * Create an empty room.
   *
   * @param id the id of the room to create
   * @return a room without title, type, users nor messages
   * @note this is the default state of any Showdown room before receiving further information from the server.
   */
  def empty(id: RoomId): JoinedRoom = JoinedRoom(
    id = id,
    title = None,
    roomType = None,
    currentTime = Timestamp.zero,
    users = Set.empty,
    chat = RoomChat.empty
  )