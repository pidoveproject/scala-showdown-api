package io.github.pidoveproject.showdown.util

import io.github.iltotore.iron.{Constraint, RefinedTypeOps}
import io.github.pidoveproject.showdown.protocol.ProtocolError
import io.github.pidoveproject.showdown.protocol.MessageDecoder.toInvalidInput

import scala.util.boundary.{Label, break}

object either:

  extension [L, R](option: Option[R])
    def getOrBreak(left: L)(using Label[Either[L, Nothing]]): R = option match
      case Some(right) => right
      case None        => break(Left(left))

  extension [L, R](either: Either[L, R])
    def getOrBreak(using Label[Either[L, Nothing]]): R = either match
      case Right(right) => right
      case Left(left)   => break(Left(left))

  extension [A, C, T](ops: RefinedTypeOps[A, C, T])
    inline def refineOrBreak(value: A)(using Constraint[A, C], Label[Either[ProtocolError, Nothing]]): T =
      ops
        .either(value)
        .toInvalidInput(value.toString)
        .getOrBreak
