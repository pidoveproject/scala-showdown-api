package io.github.projectpidove.showdown.user

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.protocol.{MessageDecoder, MessageInput}

import scala.collection.mutable.ListBuffer
import scala.util.boundary
import scala.util.boundary.break

given userListDecoder(using userDecoder: MessageDecoder[Username]): MessageDecoder[UserList] = MessageDecoder.string.mapEither: str =>
  if str.isBlank then Right(List.empty.assume)
  else boundary:
    val result = ListBuffer.empty[Username]
    for element <- str.split(",") do
      userDecoder.decode(MessageInput.fromInput(element)) match
        case Right(value) => result += value
        case Left(error) => break(Left(error))

    Right(result.toList.assume)