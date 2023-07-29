package io.github.projectpidove.showdown.protocol

import io.github.projectpidove.showdown.protocol.{ProtocolError, ProtocolInput}
import zio.Zippable
import zio.prelude.fx.ZPure

opaque type ProtocolDecoder[T] = ZPure[Nothing, ProtocolInput, ProtocolInput, Any, ProtocolError, T]

object ProtocolDecoder:

  def apply[T](program: ZPure[Nothing, ProtocolInput, ProtocolInput, Any, ProtocolError, T]): ProtocolDecoder[T] = program

  extension[T] (decoder: ProtocolDecoder[T])

    def decode(input: ProtocolInput): Either[ProtocolError, T] =
      decoder
        .provideState(input)
        .runEither

  val next: ProtocolDecoder[String] =
    for
      input <- ZPure.get[ProtocolInput]
      result <- ZPure.fromEither(input.peek)
      _ <- ZPure.set(input.skip)
    yield
      result

  given string: ProtocolDecoder[String] = next

  given boolean: ProtocolDecoder[Boolean] =
    for
      value <- next
      result <-
        if value == "1" then ZPure.succeed(true)
        else if value == "0" then ZPure.succeed(false)
        else ZPure.fromEither(value.toBooleanOption.toRight(ProtocolError.InvalidInput(value, "Not a boolean")))
    yield
      result

  given int: ProtocolDecoder[Int] =
    for
      value <- next
      result <- ZPure.fromEither(value.toIntOption.toRight(ProtocolError.InvalidInput(value, "Not a int")))
    yield
      result

  given long: ProtocolDecoder[Long] =
    for
      value <- next
      result <- ZPure.fromEither(value.toLongOption.toRight(ProtocolError.InvalidInput(value, "Not a long")))
    yield
      result

  given double: ProtocolDecoder[Double] =
    for
      value <- next
      result <- ZPure.fromEither(value.toDoubleOption.toRight(ProtocolError.InvalidInput(value, "Not a double")))
    yield
      result

  given emptyTuple: ProtocolDecoder[EmptyTuple] = ZPure.succeed(EmptyTuple)

  given nonEmptyTuple[A, T <: Tuple](using headDecoder: ProtocolDecoder[A], tailDecoder: ProtocolDecoder[T]): ProtocolDecoder[A *: T] =
    for
      head <- headDecoder
      tail <- tailDecoder
    yield
      head *: tail

