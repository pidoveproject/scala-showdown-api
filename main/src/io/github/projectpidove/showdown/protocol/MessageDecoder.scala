package io.github.projectpidove.showdown.protocol

import io.github.projectpidove.showdown.protocol.{ProtocolError, ProtocolInput}
import zio.Zippable
import zio.prelude.fx.ZPure

import scala.compiletime.{constValue, erasedValue, summonInline}
import scala.deriving.Mirror

opaque type ProtocolDecoder[T] = ZPure[Nothing, ProtocolInput, ProtocolInput, Any, ProtocolError, T]

object ProtocolDecoder:

  def apply[T](program: ZPure[Nothing, ProtocolInput, ProtocolInput, Any, ProtocolError, T]): ProtocolDecoder[T] = program

  inline def derived[T](using m: Mirror.Of[T]): ProtocolDecoder[T] = inline m match
    case p: Mirror.ProductOf[T] => derivedProduct(p, summonInline[ProtocolDecoder[p.MirroredElemTypes]])
    case s: Mirror.SumOf[T] => derivedSum(s)

  private inline def derivedProduct[T](m: Mirror.ProductOf[T], decoder: ProtocolDecoder[m.MirroredElemTypes]): ProtocolDecoder[T] =
    decoder.map(fields => m.fromProduct(fields))

  private inline def summonSumDecoder[T <: Tuple]: ProtocolDecoder[T] = inline erasedValue[T] match
    case _: EmptyTuple => ZPure.succeed(EmptyTuple).asInstanceOf[ProtocolDecoder[T]]
    case _: ((nameType, head) *: tail) =>
      val name = constValue[nameType].toString.toLowerCase
      (word(MessageName.getMessageName[head].getOrElse(name)) *> derived[head](using summonInline[Mirror.Of[head]]) <> summonSumDecoder[tail]).asInstanceOf[ProtocolDecoder[T]]

  private inline def derivedSum[T](m: Mirror.SumOf[T]): ProtocolDecoder[T] =
    summonSumDecoder[Tuple.Zip[m.MirroredElemLabels, m.MirroredElemTypes]].asInstanceOf[ProtocolDecoder[T]]

  extension[T] (decoder: ProtocolDecoder[T])

    def decode(input: ProtocolInput): Either[ProtocolError, T] =
      decoder
        .provideState(input)
        .runEither

    def filterOrElse(f: T => Boolean, error: T => ProtocolError): ProtocolDecoder[T] = decoder.filterOrElse(f)(x => ZPure.fail(error(x)))

  val next: ProtocolDecoder[String] =
    for
      input <- ZPure.get[ProtocolInput]
      result <- ZPure.fromEither(input.peek)
      _ <- ZPure.set(input.skip)
    yield
      result

  given string: ProtocolDecoder[String] = next

  def word(value: String): ProtocolDecoder[String] =
    string
      .filterOrElse((x: String) => x == value, (x: String) => ProtocolError.InvalidInput(x, s"Expected $value"))

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