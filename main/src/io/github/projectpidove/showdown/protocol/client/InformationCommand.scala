package io.github.projectpidove.showdown.protocol.client

import io.github.projectpidove.showdown.protocol.client.GroupTarget
import io.github.projectpidove.showdown.protocol.MessageEncoder
import io.github.projectpidove.showdown.FormatName
import io.github.projectpidove.showdown.room.{ChatMessage, RoomId, given}
import io.github.projectpidove.showdown.user.{Username, given}

enum InformationCommand derives MessageEncoder:
  case Groups(area: Option[GroupTarget])
  case Faq(theme: Option[String])
  case Rules(url: Option[String])
  case Intro()
  case FormatsHelp(format: Option[FormatName])
  case OtherMetas()
  case Analysis(data: Option[List[Option[String]]])
  case Punishments()
  case Calc()
  case RCalc()
  case BsCalc()
  case Git()
  case Cap()
  case RoomHelp()
  case RoomFaq(date: Option[String])
  // TODO ask raph for "!" implementation
