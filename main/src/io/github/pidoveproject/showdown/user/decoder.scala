package io.github.pidoveproject.showdown.user

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.pidoveproject.showdown.protocol.{MessageDecoder, MessageInput}

import scala.collection.mutable.ListBuffer
import scala.util.boundary
import scala.util.boundary.break

given userListDecoder(using userDecoder: MessageDecoder[User]): MessageDecoder[UserList] = MessageDecoder.string.mapEither: str =>
  if str.isBlank then Right(UserList.empty)
  else
    boundary:
      val result = ListBuffer.empty[User]
      for element <- str.split(",") do
        userDecoder.decode(MessageInput.fromInput(element)) match
          case Right(value) => result += value
          case Left(error)  => break(Left(error))

      Right(UserList.assume(result.toList))
