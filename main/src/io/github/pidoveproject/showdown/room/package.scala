package io.github.pidoveproject.showdown.room

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

opaque type RoomId = String :| Not[Blank]
object RoomId extends RefinedTypeOpsImpl[String, Not[Blank], RoomId]

opaque type ChatContent = String :| Not[Blank]
object ChatContent extends RefinedTypeOpsImpl[String, Not[Blank], ChatContent]

opaque type PopupMessage = String :| Not[Blank]
object PopupMessage extends RefinedTypeOpsImpl[String, Not[Blank], PopupMessage]

opaque type HTML = String :| Pure
object HTML extends RefinedTypeOpsImpl[String, Pure, HTML]

opaque type HighlightToken = String :| Not[Blank]
object HighlightToken extends RefinedTypeOpsImpl[String, Not[Blank], HighlightToken]
