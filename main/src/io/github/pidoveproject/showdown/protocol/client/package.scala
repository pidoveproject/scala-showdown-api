package io.github.pidoveproject.showdown.protocol.client

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

type ClientMessage = AuthCommand | BattleRoomCommand | DataCommand | GlobalCommand | HighlightCommand | InformationCommand | OptionCommand

opaque type Modifier = String :| Not[Blank]
object Modifier extends RefinedTypeOpsImpl[String, Not[Blank], Modifier]