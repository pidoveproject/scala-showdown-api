package io.github.projectpidove.showdown.protocol.server

import io.github.projectpidove.showdown.{Format, FormatCategory, GameSearch}
import io.github.projectpidove.showdown.protocol.{MessageDecoder, MessageName}
import io.github.projectpidove.showdown.protocol.MessageDecoder.given
import io.github.projectpidove.showdown.protocol.server.query.ResponseContent
import io.github.projectpidove.showdown.room.{ChallStr as ChallStrContent, *, given}
import io.github.projectpidove.showdown.user.{UserSettings, Username}

enum GlobalMessage:
  case Popup(msg: PopupMessage)
  @MessageName("pm") case PrivateMessage(sender: Username, receiver: Username, message: ChatMessage)
  case UserCount(count: Count)
  case NameTaken(name: String, message: String)
  case ChallStr(content: ChallStrContent)
  case UpdateUser(user: Username, named: Boolean, avatar: AvatarName, settings: UserSettings)
  case Formats(categories: List[FormatCategory])
  case UpdateSearch(search: GameSearch)
  case QueryResponse(content: ResponseContent)

object GlobalMessage:

  given MessageDecoder[GlobalMessage] = MessageDecoder.derived[GlobalMessage]
