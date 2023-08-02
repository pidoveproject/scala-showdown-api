package io.github.projectpidove.showdown.user

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.projectpidove.showdown.protocol.MessageDecoder
import io.github.projectpidove.showdown.protocol.ProtocolError

case class Username(name: String :| Not[Blank], rank: Option[Char])

object Username:

  given MessageDecoder[Username] = MessageDecoder.string.mapEither: str =>
    if str.length > 1 then
      val rank = Some(str.head).filterNot(_.isWhitespace)
      val name = str.tail
      for
        validName <- name.refineEither[Not[Blank]].left.map(msg => ProtocolError.InvalidInput(name, msg))
      yield
        Username(validName, rank)
    else Left(ProtocolError.InvalidInput(str, "Invalid User"))