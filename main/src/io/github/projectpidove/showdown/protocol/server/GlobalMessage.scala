package io.github.projectpidove.showdown.protocol.server

import io.github.projectpidove.showdown.{ChallStr as ChallStrContent, Count, Format, FormatCategory, GameSearch, given}
import io.github.projectpidove.showdown.protocol.{MessageDecoder, MessageName}
import io.github.projectpidove.showdown.protocol.server.query.ResponseContent
import io.github.projectpidove.showdown.room.{given, *}
import io.github.projectpidove.showdown.user.{AvatarName, UserSettings, User}

enum GlobalMessage derives MessageDecoder:
  case Popup(msg: PopupMessage)
  @MessageName("pm") case PrivateMessage(sender: User, receiver: User, message: ChatMessage)
  case UserCount(count: Count)
  case NameTaken(name: String, message: String)
  case ChallStr(content: ChallStrContent)
  case UpdateUser(user: User, named: Boolean, avatar: AvatarName, settings: UserSettings)
  case Formats(categories: List[FormatCategory])
  case UpdateSearch(search: GameSearch)
  case QueryResponse(content: ResponseContent)
