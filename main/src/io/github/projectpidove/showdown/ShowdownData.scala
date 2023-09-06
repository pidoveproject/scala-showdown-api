package io.github.projectpidove.showdown

import io.github.projectpidove.showdown.protocol.server.{GlobalMessage, RoomBoundMessage, ServerMessage}
import io.github.projectpidove.showdown.room.{ChatContent, ChatMessage, JoinedRoom, RoomId}
import io.github.projectpidove.showdown.user.{LoggedUser, Username}

/**
 * The state of the Showdown connection.
 *
 * @param userCount the number of online users
 * @param challStr the challstr of the session
 * @param loggedUser the currently logged-in user
 * @param gameSearch the current state of game matchmaking
 * @param formatCategories the formats and categories available on the connected server
 */
case class ShowdownData(
    userCount: Option[Count],
    challStr: Option[ChallStr],
    loggedUser: Option[LoggedUser],
    gameSearch: GameSearch,
    formatCategories: List[FormatCategory],
    joinedRooms: Map[RoomId, JoinedRoom]
):
  
  def isLoggedAs(name: Username): Boolean = loggedUser.exists(_.name == name)

  def getJoinedRoomOrEmpty(id: RoomId): JoinedRoom = joinedRooms.getOrElse(id, JoinedRoom.empty(id))

  /**
   * Update this data according to the passed server event/message.
   *
   * @param message the message sent by the server
   * @return a new [[ShowdownData]] updated according to the given message
   */
  def update(message: ServerMessage): ShowdownData = message match
    case GlobalMessage.UserCount(count)                          => this.copy(userCount = Some(count))
    case GlobalMessage.ChallStr(challStr)                        => this.copy(challStr = Some(challStr))
    case GlobalMessage.UpdateUser(user, named, avatar, settings) =>
      val result = loggedUser match
        case Some(value) => value.copy(name = user.name, avatar = avatar, isGuest = !named, settings = settings)
        case None => LoggedUser(user.name, avatar, !named, settings, Map.empty)

      this.copy(loggedUser = Some(result))
    case GlobalMessage.PrivateMessage(sender, receiver, message) if isLoggedAs(sender.name) || isLoggedAs(receiver.name) =>
      val key =
        if isLoggedAs(sender.name) then receiver
        else sender

      val user = loggedUser.get
      val updatedChat = user.getPrivateChat(key).withChatMessage(ChatMessage.Sent(sender, message))

      this.copy(loggedUser = Some(user.withPrivateChat(key, updatedChat)))
    case GlobalMessage.UpdateSearch(search) => this.copy(gameSearch = search)
    case GlobalMessage.Formats(categories) => this.copy(formatCategories = categories)
    case RoomBoundMessage(id, message) => this.copy(joinedRooms = joinedRooms.updated(id, getJoinedRoomOrEmpty(id).update(message)))
    case _ => this

object ShowdownData:

  /**
   * An empty [[ShowdownData]]. Typically the initial state of the data.
   */
  val empty: ShowdownData = ShowdownData(
    userCount = None,
    challStr = None,
    loggedUser = None,
    gameSearch = GameSearch.empty,
    formatCategories = List.empty,
    joinedRooms = Map.empty
  )
