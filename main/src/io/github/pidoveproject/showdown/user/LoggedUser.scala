package io.github.pidoveproject.showdown.user

import io.github.pidoveproject.showdown.FormatName
import io.github.pidoveproject.showdown.protocol.server.ServerMessage
import io.github.pidoveproject.showdown.room.RoomChat

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
  privateMessages: Map[User, RoomChat],
  challenges: Map[User, FormatName]
):
  
  def getPrivateChat(user: User): RoomChat = privateMessages.getOrElse(user, RoomChat.empty)

  def withPrivateChat(user: User, chat: RoomChat): LoggedUser = this.copy(privateMessages = privateMessages.updated(user, chat))

  def getChallenge(opponent: User): Option[FormatName] = challenges.get(opponent)

  def withChallenge(opponent: User, format: FormatName): LoggedUser = this.copy(challenges = challenges.updated(opponent, format))
  
  def removeChallenge(opponent: User): LoggedUser = this.copy(challenges = challenges.removed(opponent))
