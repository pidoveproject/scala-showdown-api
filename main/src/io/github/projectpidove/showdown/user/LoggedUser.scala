package io.github.projectpidove.showdown.user

import io.github.projectpidove.showdown.protocol.server.ServerMessage
import io.github.projectpidove.showdown.room.RoomChat

/**
 * Represent a logged-in user. Typically the current user when connecting to Showdown.
 *
 * @param name the name of the user
 * @param avatar the avatar representing this user
 * @param isGuest whether this user is a guest or has a registered account
 * @param settings this user's settings
 */
case class LoggedUser(
  name: Username,
  avatar: AvatarName,
  isGuest: Boolean,
  settings: UserSettings,
  privateMessages: Map[User, RoomChat]
):
  
  def getPrivateChat(user: User): RoomChat = privateMessages.getOrElse(user, RoomChat.empty)

  def withPrivateChat(user: User, chat: RoomChat): LoggedUser = this.copy(privateMessages = privateMessages.updated(user, chat))
