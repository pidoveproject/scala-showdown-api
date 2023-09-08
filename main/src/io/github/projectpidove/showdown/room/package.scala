package io.github.projectpidove.showdown.room

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.projectpidove.showdown.protocol.{MessageDecoder, MessageInput, ProtocolError}
import io.github.projectpidove.showdown.user.User
import scala.collection.mutable.ListBuffer
import scala.util.boundary, boundary.break

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
