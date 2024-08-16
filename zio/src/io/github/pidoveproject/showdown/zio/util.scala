package io.github.pidoveproject.showdown.zio

import io.github.iltotore.iron.{Constraint, RefinedTypeOpsImpl}
import io.github.pidoveproject.showdown.protocol.ProtocolError
import zio.*

extension [R, A](program: ZIO[R, Throwable, A])
  def toProtocolZIO: ZIO[R, ProtocolError, A] = program.mapError(ProtocolError.Thrown.apply)

extension [A, C, T](ops: RefinedTypeOpsImpl[A, C, T])
  inline def applyZIO(value: A)(using constraint: Constraint[A, C]): IO[ProtocolError, T] =
    if constraint.test(value) then ZIO.succeed(value.asInstanceOf[T])
    else ZIO.fail(ProtocolError.InvalidInput(value.toString, constraint.message))
