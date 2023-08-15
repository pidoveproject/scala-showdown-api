package io.github.projectpidove.showdown.protocol

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.protocol.{MessageInput, ProtocolError}
import io.github.projectpidove.showdown.util.UnionTypeMirror
import zio.Zippable
import zio.json.*
import zio.prelude.fx.ZPure

import scala.compiletime.{constValue, erasedValue, summonInline}
import scala.deriving.Mirror

class MessageDecoder[+T](zpure: ZPure[Nothing, MessageInput, MessageInput, Any, ProtocolError, T]):

  def toZPure: ZPure[Nothing, MessageInput, MessageInput, Any, ProtocolError, T] = zpure

  def decodeZPure(input: MessageInput): ZPure[Nothing, Unit, MessageInput, Any, ProtocolError, T] =
    zpure.provideState(input)

  def decode(input: MessageInput): Either[ProtocolError, T] =
    zpure
      .provideState(input)
      .runEither

  def filterOrElse(f: T => Boolean, error: T => ProtocolError): MessageDecoder[T] =
    MessageDecoder(zpure.filterOrElse(f)(x => ZPure.fail(error(x))))

  def map[A](f: T => A): MessageDecoder[A] = MessageDecoder(zpure.map(f))

  def flatMap[A](f: T => MessageDecoder[A]): MessageDecoder[A] = MessageDecoder(zpure.flatMap(x => f(x).toZPure))

  def *>[A](other: => MessageDecoder[A]): MessageDecoder[A] = flatMap(_ => other)

  def mapEither[A](f: T => Either[ProtocolError, A]): MessageDecoder[A] = flatMap(x => MessageDecoder.fromEither(f(x)))

  def repeatUntilInput(f: MessageInput => Boolean): MessageDecoder[List[T]] =

    def rec(): ZPure[Nothing, MessageInput, MessageInput, Any, ProtocolError, List[T]] =
      zpure.zip(ZPure.get[MessageInput]).flatMap: (a, s) =>
        if f(s) then ZPure.succeed(a :: Nil)
        else rec().map(a :: _)

    MessageDecoder(rec())

  def repeatUntilCurrent(f: String => Boolean): MessageDecoder[List[T]] =
    repeatUntilInput(input => input.peek.forall(f))

  def repeatUntilEnd: MessageDecoder[List[T]] = repeatUntilInput(_.exhausted)

  def repeatUntilFail: MessageDecoder[List[T]] =

    def rec(): ZPure[Nothing, MessageInput, MessageInput, Any, ProtocolError, List[T]] =
      val concat =
        for
          head <- zpure
          tail <- rec()
        yield head :: tail

      concat.catchAll(_ => ZPure.succeed(Nil))

    MessageDecoder(rec())

  def orElse[A](other: MessageDecoder[A]): MessageDecoder[T | A] = MessageDecoder(zpure.orElse(other.toZPure))

  def <>[A](other: MessageDecoder[A]): MessageDecoder[T | A] = orElse(other)

object MessageDecoder:

  def succeed[T](value: T): MessageDecoder[T] = MessageDecoder(ZPure.succeed(value))

  def fail(error: ProtocolError): MessageDecoder[Nothing] = MessageDecoder(ZPure.fail(error))

  def fromOption[T](value: Option[T]): MessageDecoder[T] = fromEither(value.toRight(ProtocolError.Miscellaneous("Missing value")))

  def fromEither[T](value: Either[ProtocolError, T]): MessageDecoder[T] = MessageDecoder(ZPure.fromEither(value))

  def attempt[T](value: => T): MessageDecoder[T] = attemptOrElse(value, ProtocolError.Thrown.apply)

  def attemptOrElse[T](value: => T, error: Throwable => ProtocolError): MessageDecoder[T] =
    MessageDecoder:
      ZPure
        .attempt(value)
        .mapError(error)

  inline def derived[T](using m: Mirror.Of[T]): MessageDecoder[T] = inline m match
    case p: Mirror.ProductOf[T] => derivedProduct(p, summonInline[MessageDecoder[p.MirroredElemTypes]])
    case s: Mirror.SumOf[T]     => derivedSum(s)

  private inline def derivedProduct[T](m: Mirror.ProductOf[T], decoder: MessageDecoder[m.MirroredElemTypes]): MessageDecoder[T] =
    decoder.map(fields => m.fromProduct(fields))

  private inline def namesOrDefault(names: Seq[String], default: String): MessageDecoder[String] =
    if names.isEmpty then word(default)
    else oneOf(names*)

  private inline def summonSumDecoder[T <: Tuple]: MessageDecoder[T] = inline erasedValue[T] match
    case _: EmptyTuple => next.mapEither(x => Left(ProtocolError.InvalidInput(x, "Invalid enum case")))
    case _: ((nameType, head) *: EmptyTuple) =>
      val name = constValue[nameType].toString.toLowerCase
      (namesOrDefault(MessageName.getMessageNames[head], name) *> derived[head](using summonInline[Mirror.Of[head]])).asInstanceOf[MessageDecoder[T]]
    case _: ((nameType, head) *: tail) =>
      val name = constValue[nameType].toString.toLowerCase
      (namesOrDefault(MessageName.getMessageNames[head], name) *> derived[head](using summonInline[Mirror.Of[head]]) <> summonSumDecoder[
        tail
      ]).asInstanceOf[MessageDecoder[T]]

  private inline def derivedSum[T](m: Mirror.SumOf[T]): MessageDecoder[T] =
    summonSumDecoder[Tuple.Zip[m.MirroredElemLabels, m.MirroredElemTypes]].asInstanceOf[MessageDecoder[T]]

  private inline def summonUnionDecoder[T <: Tuple]: MessageDecoder[T] = inline erasedValue[T] match
    case _: EmptyTuple => next.mapEither(x => Left(ProtocolError.InvalidInput(x, "Cannot decode union")))
    case _: (head *: tail) => (summonInline[MessageDecoder[head]] <> summonUnionDecoder[tail]).asInstanceOf[MessageDecoder[T]]

  inline given derivedUnion[T](using m: UnionTypeMirror[T]): MessageDecoder[T] =
    summonUnionDecoder[m.ElementTypes].asInstanceOf[MessageDecoder[T]]

  extension (value: String)
    
    def decode[T](using decoder: MessageDecoder[T]): Either[ProtocolError, T] =
      decoder.decode(MessageInput.fromInput(value))

  extension [R](either: Either[String, R])
    def toInvalidInput(input: String): Either[ProtocolError, R] =
      either.left.map(msg => ProtocolError.InvalidInput(input, msg))

  val next: MessageDecoder[String] =
    MessageDecoder:
      for
        input <- ZPure.get[MessageInput]
        result <- ZPure.fromEither(input.peek)
        _ <- ZPure.set(input.skip)
      yield result

  inline given ironType[A, C](using inline decoder: MessageDecoder[A], constraint: Constraint[A, C]): MessageDecoder[A :| C] =
    decoder
      .filterOrElse(constraint.test(_), x => ProtocolError.InvalidInput(x.toString, constraint.message))
      .map[A :| C](_.assume[C])

  inline given newtype[T](using mirror: RefinedTypeOps.Mirror[T]): MessageDecoder[T] =
    summonInline[MessageDecoder[mirror.IronType]].asInstanceOf[MessageDecoder[T]]

  given string: MessageDecoder[String] = next

  def word(value: String): MessageDecoder[String] =
    string
      .filterOrElse((x: String) => x == value, (x: String) => ProtocolError.InvalidInput(x, s"Expected $value"))

  def oneOf(values: String*): MessageDecoder[String] =
    string
      .filterOrElse(values.contains, (x: String) => ProtocolError.InvalidInput(x, s"Expected one of: ${values.mkString(",")}"))

  def fromJson[A: JsonDecoder]: MessageDecoder[A] =
    string.mapEither(x => x.fromJson[A].left.map(msg => ProtocolError.InvalidInput(x, msg)))

  given boolean: MessageDecoder[Boolean] =
    for
      value <- next
      result <-
        if value == "1" then MessageDecoder.succeed(true)
        else if value == "0" then MessageDecoder.succeed(false)
        else MessageDecoder.fromEither(value.toBooleanOption.toRight(ProtocolError.InvalidInput(value, "Not a boolean")))
    yield result

  given int: MessageDecoder[Int] =
    for
      value <- next
      result <- MessageDecoder.fromEither(value.toIntOption.toRight(ProtocolError.InvalidInput(value, "Not a int")))
    yield result

  given long: MessageDecoder[Long] =
    for
      value <- next
      result <- MessageDecoder.fromEither(value.toLongOption.toRight(ProtocolError.InvalidInput(value, "Not a long")))
    yield result

  given double: MessageDecoder[Double] =
    for
      value <- next
      result <- MessageDecoder.fromEither(value.toDoubleOption.toRight(ProtocolError.InvalidInput(value, "Not a double")))
    yield result

  given emptyTuple: MessageDecoder[EmptyTuple] = MessageDecoder.succeed(EmptyTuple)

  given nonEmptyTuple[A, T <: Tuple](using headDecoder: MessageDecoder[A], tailDecoder: MessageDecoder[T]): MessageDecoder[A *: T] =
    for
      head <- headDecoder
      tail <- tailDecoder
    yield head *: tail

  given option[A](using decoder: MessageDecoder[A]): MessageDecoder[Option[A]] =
    decoder.map(Some.apply) <> MessageDecoder.succeed(None)

  given list[A](using decoder: MessageDecoder[A]): MessageDecoder[List[A]] = decoder.repeatUntilEnd
