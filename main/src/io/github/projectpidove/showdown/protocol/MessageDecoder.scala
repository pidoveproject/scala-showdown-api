package io.github.projectpidove.showdown.protocol

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.protocol.{MessageInput, ProtocolError}
import io.github.projectpidove.showdown.util.UnionTypeMirror
import zio.Zippable
import zio.json.*
import zio.prelude.fx.ZPure

import scala.compiletime.{constValue, erasedValue, summonInline}
import scala.deriving.Mirror

opaque type MessageDecoder[T] = ZPure[Nothing, MessageInput, MessageInput, Any, ProtocolError, T]

object MessageDecoder:

  def apply[T](program: ZPure[Nothing, MessageInput, MessageInput, Any, ProtocolError, T]): MessageDecoder[T] = program

  def fromOption[T](value: Option[T]): MessageDecoder[T] = fromEither(value.toRight(ProtocolError.Miscellaneous("Missing value")))

  def fromEither[T](value: Either[ProtocolError, T]): MessageDecoder[T] = ZPure.fromEither(value)

  def attempt[T](value: => T): MessageDecoder[T] = attemptOrElse(value, ProtocolError.Thrown.apply)

  def attemptOrElse[T](value: => T, error: Throwable => ProtocolError): MessageDecoder[T] =
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
  
  extension [T](decoder: MessageDecoder[T])

    def decodeZPure(input: MessageInput): ZPure[Nothing, Unit, MessageInput, Any, ProtocolError, T] =
      decoder.provideState(input)

    def decode(input: MessageInput): Either[ProtocolError, T] =
      decoder
        .provideState(input)
        .runEither

    def filterOrElse(f: T => Boolean, error: T => ProtocolError): MessageDecoder[T] = decoder.filterOrElse(f)(x => ZPure.fail(error(x)))

    def map[A](f: T => A): MessageDecoder[A] = decoder.map(f)

    def flatMap[A](f: T => MessageDecoder[A]): MessageDecoder[A] = decoder.flatMap(f)

    def mapEither[A](f: T => Either[ProtocolError, A]): MessageDecoder[A] = decoder.flatMap(x => ZPure.fromEither(f(x)))

    def repeatUntilInput(f: MessageInput => Boolean): MessageDecoder[List[T]] =
      decoder.zip(ZPure.get[MessageInput]).flatMap: (a, s) =>
        if f(s) then ZPure.succeed(a :: Nil)
        else repeatUntilInput(f).map(a :: _)

    def repeatUntilCurrent(f: String => Boolean): MessageDecoder[List[T]] =
      repeatUntilInput(input => input.peek.forall(f))

    def repeatUntilEnd: MessageDecoder[List[T]] = repeatUntilInput(_.exhausted)

    def repeatUntilFail: MessageDecoder[List[T]] =
      val concat =
        for
          head <- decoder
          tail <- decoder.repeatUntilFail
        yield head :: tail

      concat.catchAll(_ => ZPure.succeed(Nil))

    def orElse[A](other: MessageDecoder[A]): MessageDecoder[T | A] = decoder.orElse(other)

    def <>[A](other: MessageDecoder[A]): MessageDecoder[T | A] = decoder.orElse(other)

  extension [R](either: Either[String, R])
    def toInvalidInput(input: String): Either[ProtocolError, R] =
      either.left.map(msg => ProtocolError.InvalidInput(input, msg))

  val next: MessageDecoder[String] =
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
        if value == "1" then ZPure.succeed(true)
        else if value == "0" then ZPure.succeed(false)
        else ZPure.fromEither(value.toBooleanOption.toRight(ProtocolError.InvalidInput(value, "Not a boolean")))
    yield result

  given int: MessageDecoder[Int] =
    for
      value <- next
      result <- ZPure.fromEither(value.toIntOption.toRight(ProtocolError.InvalidInput(value, "Not a int")))
    yield result

  given long: MessageDecoder[Long] =
    for
      value <- next
      result <- ZPure.fromEither(value.toLongOption.toRight(ProtocolError.InvalidInput(value, "Not a long")))
    yield result

  given double: MessageDecoder[Double] =
    for
      value <- next
      result <- ZPure.fromEither(value.toDoubleOption.toRight(ProtocolError.InvalidInput(value, "Not a double")))
    yield result

  given emptyTuple: MessageDecoder[EmptyTuple] = ZPure.succeed(EmptyTuple)

  given nonEmptyTuple[A, T <: Tuple](using headDecoder: MessageDecoder[A], tailDecoder: MessageDecoder[T]): MessageDecoder[A *: T] =
    for
      head <- headDecoder
      tail <- tailDecoder
    yield head *: tail

  given option[A](using decoder: MessageDecoder[A]): MessageDecoder[Option[A]] =
    decoder.map(Some.apply) <> ZPure.succeed(None)

  given list[A](using decoder: MessageDecoder[A]): MessageDecoder[List[A]] = decoder.repeatUntilEnd
