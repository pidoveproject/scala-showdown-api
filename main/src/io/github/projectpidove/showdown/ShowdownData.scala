package io.github.projectpidove.showdown

import io.github.projectpidove.showdown.protocol.server.query.ResponseContent.UserDetails
import io.github.projectpidove.showdown.protocol.server.query.{ResponseContent, UserInfo}
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
 * @param joinedRooms the room the logged user is currently connected to
 * @param userDetails the details of other users
 */
case class ShowdownData(
    userCount: Option[Count],
    challStr: Option[ChallStr],
    loggedUser: Option[LoggedUser],
    gameSearch: GameSearch,
    formatCategories: List[FormatCategory],
    joinedRooms: Map[RoomId, JoinedRoom],
    userDetails: Map[String, UserInfo]
):

  /**
   * Check if the client is logged with the given name.
   *
   * @param name the name to check
   * @return whether the current user has the name `name` or not
   */
  def isLoggedAs(name: Username): Boolean = loggedUser.exists(_.name == name)

  /**
   * Get the room information from an id.
   *
   * @param id the id of the room to get
   * @return the room with the given id or an empty instance if it does not exist
   */
  def getJoinedRoomOrEmpty(id: RoomId): JoinedRoom = joinedRooms.getOrElse(id, JoinedRoom.empty(id))

  /**
   * Get the details of a user.
   *
   * @param id the id of the user
   * @return the details of the user with the given id or `None` if absent
   */
  def getUserDetails(id: String): Option[UserInfo] = userDetails.get(id)

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
    case GlobalMessage.QueryResponse(ResponseContent.UserDetails(info)) => this.copy(userDetails = userDetails.updated(info.id, info))
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
    joinedRooms = Map.empty,
    userDetails = Map.empty
  )
