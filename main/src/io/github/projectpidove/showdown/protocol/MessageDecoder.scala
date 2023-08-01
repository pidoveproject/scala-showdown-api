package io.github.projectpidove.showdown.protocol

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.protocol.{MessageInput, ProtocolError}
import zio.Zippable
import zio.prelude.fx.ZPure

import scala.compiletime.{constValue, erasedValue, summonInline}
import scala.deriving.Mirror

opaque type MessageDecoder[T] = ZPure[Nothing, MessageInput, MessageInput, Any, ProtocolError, T]

object MessageDecoder:

  def apply[T](program: ZPure[Nothing, MessageInput, MessageInput, Any, ProtocolError, T]): MessageDecoder[T] = program

  inline def derived[T](using m: Mirror.Of[T]): MessageDecoder[T] = inline m match
    case p: Mirror.ProductOf[T] => derivedProduct(p, summonInline[MessageDecoder[p.MirroredElemTypes]])
    case s: Mirror.SumOf[T] => derivedSum(s)

  private inline def derivedProduct[T](m: Mirror.ProductOf[T], decoder: MessageDecoder[m.MirroredElemTypes]): MessageDecoder[T] =
    decoder.map(fields => m.fromProduct(fields))

  private inline def summonSumDecoder[T <: Tuple]: MessageDecoder[T] = inline erasedValue[T] match
    case _: EmptyTuple => next.mapEither(x => Left(ProtocolError.InvalidInput(x, "Invalid enum case")))
    case _: ((nameType, head) *: EmptyTuple) =>
      val name = constValue[nameType].toString.toLowerCase
      (word(MessageName.getMessageName[head].getOrElse(name)) *> derived[head](using summonInline[Mirror.Of[head]])).asInstanceOf[MessageDecoder[T]]
    case _: ((nameType, head) *: tail) =>
      val name = constValue[nameType].toString.toLowerCase
      (word(MessageName.getMessageName[head].getOrElse(name)) *> derived[head](using summonInline[Mirror.Of[head]]) <> summonSumDecoder[tail]).asInstanceOf[MessageDecoder[T]]

  private inline def derivedSum[T](m: Mirror.SumOf[T]): MessageDecoder[T] =
    summonSumDecoder[Tuple.Zip[m.MirroredElemLabels, m.MirroredElemTypes]].asInstanceOf[MessageDecoder[T]]

  extension[T] (decoder: MessageDecoder[T])

    def decode(input: MessageInput): Either[ProtocolError, T] =
      decoder
        .provideState(input)
        .runEither

    def filterOrElse(f: T => Boolean, error: T => ProtocolError): MessageDecoder[T] = decoder.filterOrElse(f)(x => ZPure.fail(error(x)))
    
    def map[A](f: T => A): MessageDecoder[A] = decoder.map(f)

    def flatMap[A](f: T => MessageDecoder[A]): MessageDecoder[A] = decoder.flatMap(f)
    
    def mapEither[A](f: T => Either[ProtocolError, A]): MessageDecoder[A] = decoder.flatMap(x => ZPure.fromEither(f(x)))

  val next: MessageDecoder[String] =
    for
      input <- ZPure.get[MessageInput]
      result <- ZPure.fromEither(input.peek)
      _ <- ZPure.set(input.skip)
    yield
      result

  inline given ironType[A, C](using inline decoder: MessageDecoder[A], constraint: Constraint[A, C]): MessageDecoder[A :| C] =
    decoder
      .filterOrElse(constraint.test(_), x => ProtocolError.InvalidInput(x.toString, constraint.message))
      .map[A :| C](_.assume[C])

  given string: MessageDecoder[String] = next

  def word(value: String): MessageDecoder[String] =
    string
      .filterOrElse((x: String) => x == value, (x: String) => ProtocolError.InvalidInput(x, s"Expected $value"))

  given boolean: MessageDecoder[Boolean] =
    for
      value <- next
      result <-
        if value == "1" then ZPure.succeed(true)
        else if value == "0" then ZPure.succeed(false)
        else ZPure.fromEither(value.toBooleanOption.toRight(ProtocolError.InvalidInput(value, "Not a boolean")))
    yield
      result

  given int: MessageDecoder[Int] =
    for
      value <- next
      result <- ZPure.fromEither(value.toIntOption.toRight(ProtocolError.InvalidInput(value, "Not a int")))
    yield
      result

  given long: MessageDecoder[Long] =
    for
      value <- next
      result <- ZPure.fromEither(value.toLongOption.toRight(ProtocolError.InvalidInput(value, "Not a long")))
    yield
      result

  given double: MessageDecoder[Double] =
    for
      value <- next
      result <- ZPure.fromEither(value.toDoubleOption.toRight(ProtocolError.InvalidInput(value, "Not a double")))
    yield
      result

  given emptyTuple: MessageDecoder[EmptyTuple] = ZPure.succeed(EmptyTuple)

  given nonEmptyTuple[A, T <: Tuple](using headDecoder: MessageDecoder[A], tailDecoder: MessageDecoder[T]): MessageDecoder[A *: T] =
    for
      head <- headDecoder
      tail <- tailDecoder
    yield
      head *: tail