package io.github.pidoveproject.showdown.room

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

opaque type RoomId = String :| Not[Blank]
object RoomId extends RefinedTypeOps[String, Not[Blank], RoomId]

opaque type ChatContent = String :| Not[Blank]
object ChatContent extends RefinedTypeOps[String, Not[Blank], ChatContent]

opaque type PopupMessage = String :| Not[Blank]
object PopupMessage extends RefinedTypeOps[String, Not[Blank], PopupMessage]

opaque type HTML = String :| Pure
object HTML extends RefinedTypeOps[String, Pure, HTML]

opaque type HighlightToken = String :| Not[Blank]
object HighlightToken extends RefinedTypeOps[String, Not[Blank], HighlightToken]
