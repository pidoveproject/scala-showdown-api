package io.github.projectpidove.showdown.protocol

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.protocol.{MessageInput, ProtocolError}
import io.github.projectpidove.showdown.util.UnionTypeMirror
import zio.Zippable
import zio.json.*
import zio.prelude.fx.ZPure

import scala.compiletime.{constValue, erasedValue, error, summonInline}
import scala.deriving.Mirror

/**
 * A message decoder.
 *
 * @param zpure the internal logic for decoding a message
 * @tparam T the type to deserialize
 */
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

  /**
   * Create an always-succeeding decoder.
   *
   * @param value the result of the decoder
   * @tparam T the result type
   * @return a decoder that always succeeds with the passed value
   */
  def succeed[T](value: T): MessageDecoder[T] = MessageDecoder(ZPure.succeed(value))

  /**
   * Create an always-failing decoder.
   *
   * @param error the error of the decoder
   * @return a decoder that always fail with the passed error
   */
  def fail(error: ProtocolError): MessageDecoder[Nothing] = MessageDecoder(ZPure.fail(error))

  /**
   * Create a decoder from an optional value.
   *
   * @param value the optional value to pass if exists.
   * @tparam T the result type of the decoder
   * @return a decoder succeeding with the current value or failing if the passed value is `None`
   */
  def fromOption[T](value: Option[T]): MessageDecoder[T] = fromEither(value.toRight(ProtocolError.Miscellaneous("Missing value")))

  /**
   * Create a decoder from an either.
   *
   * @param value the error or succeeding result
   * @tparam T the result type
   * @return a decoder succeeding with the right result or failing with the left error
   */
  def fromEither[T](value: Either[ProtocolError, T]): MessageDecoder[T] = MessageDecoder(ZPure.fromEither(value))

  /**
   * A decoder attempting to evaluate the given result.
   *
   * @param value the result to evaluate
   * @tparam T the result type
   * @return a decoder succeeding with the passed result or failing if the result evaluation failed
   * @see [[attemptOrElse]]
   */
  def attempt[T](value: => T): MessageDecoder[T] = attemptOrElse(value, ProtocolError.Thrown.apply)

  /**
   * A decoder attempting to evaluate the given result.
   *
   * @param value the result to evaluate
   * @param error the function to transform the thrown error into a [[ProtocolError]]
   * @tparam T the result type
   * @return a decoder succeeding with the passed result or failing with the given error if the result evaluation failed
   * @see [[attempt]]
   */
  def attemptOrElse[T](value: => T, error: Throwable => ProtocolError): MessageDecoder[T] =
    MessageDecoder:
      ZPure
        .attempt(value)
        .mapError(error)

  /**
   * Derive a decoder from a case class or an enum.
   *
   * @param m the mirror representing the result type
   * @tparam T the result type
   * @return a decoder automatically generated for the type `T`
   */
  inline def derived[T](using m: Mirror.Of[T]): MessageDecoder[T] = inline m match
    case p: Mirror.ProductOf[T] => derivedProduct(p, summonInline[MessageDecoder[p.MirroredElemTypes]])
    case s: Mirror.SumOf[T]     => derivedSum(s)

  private inline def derivedProduct[T](m: Mirror.ProductOf[T], decoder: MessageDecoder[m.MirroredElemTypes]): MessageDecoder[T] =
    decoder.map(fields => m.fromProduct(fields))

  private inline def namesOrDefault(names: Seq[String], default: String): MessageDecoder[String] =
    if names.isEmpty then word(default)
    else oneOf(names*)

//  private inline def summonSumDecoder[T <: Tuple]: MessageDecoder[T] = inline erasedValue[T] match
//    case _: EmptyTuple => next.mapEither(x => Left(ProtocolError.InvalidInput(x, "Invalid enum case")))
//    case _: ((nameType, head) *: EmptyTuple) =>
//      val name = constValue[nameType].toString.toLowerCase
//      (namesOrDefault(MessageName.getMessageNames[head], name) *> derived[head](using summonInline[Mirror.Of[head]])).asInstanceOf[MessageDecoder[T]]
//    case _: ((nameType, head) *: tail) =>
//      val name = constValue[nameType].toString.toLowerCase
//      (namesOrDefault(MessageName.getMessageNames[head], name) *> derived[head](using summonInline[Mirror.Of[head]]) <> summonSumDecoder[
//        tail
//      ]).asInstanceOf[MessageDecoder[T]]

  private inline def summonDecoderMap[A, T <: Tuple]: Map[MessageDecoder[?], MessageDecoder[A]] = inline erasedValue[T] match
    case _: EmptyTuple => Map.empty
    case _: ((nameType, head) *: tail) =>
      val name = constValue[nameType].toString.toLowerCase
      val keyDecoder = namesOrDefault(MessageName.getMessageNames[head], name)
      val caseDecoder = derived[head](using summonInline[Mirror.Of[head]]).asInstanceOf[MessageDecoder[A]]
      Map(keyDecoder -> caseDecoder) ++ summonDecoderMap[A, tail]

  private inline def derivedSum[T](m: Mirror.SumOf[T]): MessageDecoder[T] =
    val casesMap = summonDecoderMap[T, Tuple.Zip[m.MirroredElemLabels, m.MirroredElemTypes]]

    val decoder =
      for
        input <- ZPure.get[MessageInput]
        decoderOption =casesMap.collectFirst(Function.unlift: (keyDecoder, caseDecoder) =>
            val result = keyDecoder.toZPure.runAll(input)
            result match
              case (_, Right((newInput, _))) => Some(caseDecoder.toZPure.provideState(newInput))
              case (_, Left(_)) => None
        )
        result <-
          decoderOption
            .getOrElse(ZPure.fail(input.peek.map(in => ProtocolError.InvalidInput(in, "No suitable case found")).merge))
      yield
        result

    MessageDecoder(decoder)

  private inline def summonUnionDecoder[T <: Tuple]: MessageDecoder[T] = inline erasedValue[T] match
    case _: EmptyTuple     => next.mapEither(x => Left(ProtocolError.InvalidInput(x, "Cannot decode union")))
    case _: (head *: tail) => (summonInline[MessageDecoder[head]] <> summonUnionDecoder[tail]).asInstanceOf[MessageDecoder[T]]

  /**
   * Derive a decoder from an union type.
   *
   * @param m the mirror representing the union type
   * @tparam T the result type (union)
   * @return a decoder automatically generated for the type `T`
   */
  inline given derivedUnion[T](using m: UnionTypeMirror[T]): MessageDecoder[T] =
    summonUnionDecoder[m.ElementTypes].asInstanceOf[MessageDecoder[T]]

  extension (value: String)
    /**
     * Decode the value.
     *
     * @return either a succeeding result or an error if the decoding failed
     */
    def decode[T](using decoder: MessageDecoder[T]): Either[ProtocolError, T] =
      decoder.decode(MessageInput.fromInput(value))

  extension [R](either: Either[String, R])
    /**
     * Transforms the error message of this Either to a ProtocolError
     */
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

  /**
   * A decoder accepting a keyword.
   *
   * @param value the keyword to decode
   * @return a decoder resulting in the passed keyword as String
   */
  def word(value: String): MessageDecoder[String] =
    string
      .filterOrElse((x: String) => x == value, (x: String) => ProtocolError.InvalidInput(x, s"Expected $value"))

  /**
   * A decoder accepting one of the given keywords.
   *
   * @param values the accepted keywords
   * @return a decoder resulting in the parsed keyword
   */
  def oneOf(values: String*): MessageDecoder[String] =
    string
      .filterOrElse(values.contains, (x: String) => ProtocolError.InvalidInput(x, s"Expected one of: ${values.mkString(",")}"))

  /**
   * Create a decoder from a [[JsonDecoder]]
   */
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
