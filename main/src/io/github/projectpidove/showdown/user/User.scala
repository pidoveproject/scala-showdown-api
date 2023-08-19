package io.github.projectpidove.showdown.user

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.projectpidove.showdown.protocol.MessageDecoder
import MessageDecoder.*
import io.github.projectpidove.showdown.protocol.ProtocolError

/**
 * Represents a public user as seen from others.
 *
 * @param name this user's name
 * @param rank this user's rank if exists
 */
case class User(name: Username, rank: Option[Char])

object User:

  given MessageDecoder[User] = MessageDecoder.string.mapEither: str =>
    if str.length > 1 then
      val rank = Some(str.head).filterNot(_.isWhitespace)
      val name = str.tail
      for
        validName <- Username.either(name).toInvalidInput(name)
      yield User(validName, rank)
    else Left(ProtocolError.InvalidInput(str, "Invalid User"))
