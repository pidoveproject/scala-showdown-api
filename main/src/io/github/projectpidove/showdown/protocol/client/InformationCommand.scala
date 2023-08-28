package io.github.projectpidove.showdown.protocol.client

import io.github.projectpidove.showdown.{FormatName, Generation}
import io.github.projectpidove.showdown.protocol.MessageEncoder
import io.github.projectpidove.showdown.team.{SpeciesName, Tier}

enum InformationCommand derives MessageEncoder:
  case Groups(area: Option[GroupTarget])
  case Faq(theme: Option[String])
  case Rules(url: Option[String])
  case Intro
  case FormatsHelp(format: Option[FormatName])
  case OtherMetas
  case Analysis(pokemon: SpeciesName, generation: Option[Generation], tier: Option[Tier])
  case Punishments
  case Calc
  case RCalc
  case BsCalc
  case Git
  case Cap
  case RoomHelp
  case RoomFaq(topic: Option[String])
