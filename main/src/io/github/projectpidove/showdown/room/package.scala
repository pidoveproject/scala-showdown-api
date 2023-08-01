package io.github.projectpidove.showdown.room

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.projectpidove.showdown.protocol.{MessageDecoder, MessageInput, ProtocolError}
import io.github.projectpidove.showdown.user.Username
import scala.collection.mutable.ListBuffer
import scala.util.boundary, boundary.break

type RoomTitle = String :| Not[Blank]
object RoomTitle extends RefinedTypeOpsImpl[String, Not[Blank], RoomTitle]

type UserList = List[Username] :| Pure
object UserList extends RefinedTypeOpsImpl[List[Username], Blank, UserList]:

  def apply(names: Username*): UserList = List(names: _*).assume

given userListDecoder(using userDecoder: MessageDecoder[Username]): MessageDecoder[UserList] = MessageDecoder.string.mapEither: str =>
  if str.isBlank then Right(List.empty.assume)
  else boundary:
    val result = ListBuffer.empty[Username]
    for element <- str.split(",") do
      userDecoder.decode(MessageInput.fromInput(element)) match
        case Right(value) => result += value
        case Left(error) => break(Left(error))

    Right(result.toList.assume)