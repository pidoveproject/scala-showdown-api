package io.github.projectpidove.showdown

import io.github.projectpidove.showdown.protocol.server.{GlobalMessage, ServerMessage}
import io.github.projectpidove.showdown.user.LoggedUser

case class ShowdownData(
  userCount: Option[Count],
  challStr: Option[ChallStr],
  loggedUser: Option[LoggedUser],
  gameSearch: GameSearch,
  formatCategories: List[FormatCategory]
):

  def update(message: ServerMessage): ShowdownData = message match
    case GlobalMessage.UserCount(count) => this.copy(userCount = Some(count))
    case GlobalMessage.ChallStr(challStr) => this.copy(challStr = Some(challStr))
    case GlobalMessage.UpdateUser(user, named, avatar, settings) => this.copy(loggedUser = Some(LoggedUser(user.name, avatar, !named, settings)))
    case GlobalMessage.UpdateSearch(search) => this.copy(gameSearch = search)
    case GlobalMessage.Formats(categories) => this.copy(formatCategories = categories)
    case _ => this

object ShowdownData:
  
  val empty: ShowdownData = ShowdownData(
    userCount = None,
    challStr = None,
    loggedUser = None,
    gameSearch = GameSearch.empty,
    formatCategories = List.empty
  )