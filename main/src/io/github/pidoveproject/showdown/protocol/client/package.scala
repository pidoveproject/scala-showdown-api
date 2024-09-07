package io.github.pidoveproject.showdown.protocol.client

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.pidoveproject.showdown.protocol.MessageEncoder

type ClientMessage = AuthCommand | BattleRoomCommand | DataCommand | GlobalCommand | HighlightCommand | InformationCommand | OptionCommand

opaque type Modifier = String :| Not[Blank]
object Modifier extends RefinedTypeOps[String, Not[Blank], Modifier]

object ClientMessage:

  def encoder: MessageEncoder[ClientMessage] = MessageEncoder.derivedUnion[ClientMessage]
