package io.github.projectpidove.showdown.room

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.projectpidove.showdown.protocol.{MessageDecoder, MessageInput, ProtocolError}
import io.github.projectpidove.showdown.user.Username
import scala.collection.mutable.ListBuffer
import scala.util.boundary, boundary.break

type RoomId = String :| Not[Blank]
object RoomId extends RefinedTypeOpsImpl[String, Not[Blank], RoomId]

opaque type ChatMessage = String :| Not[Blank]
object ChatMessage extends RefinedTypeOpsImpl[String, Not[Blank], ChatMessage]

type UserList = List[Username] :| Pure
object UserList extends RefinedTypeOpsImpl[List[Username], Blank, UserList]:

  def apply(names: Username*): UserList = List(names: _*).assume
    
type HTML = String :| Pure
object HTML extends RefinedTypeOpsImpl[String, Pure, HTML]

type HighlightToken = String :| Not[Blank]
object HighlightToken extends RefinedTypeOpsImpl[String, Not[Blank], HighlightToken]

type Timestamp = Long :| Positive
object Timestamp extends RefinedTypeOpsImpl[Long, Positive, Timestamp]