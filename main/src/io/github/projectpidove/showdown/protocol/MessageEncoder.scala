package io.github.projectpidove.showdown.protocol

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.util.UnionTypeMirror

import scala.collection.mutable.ListBuffer
import scala.compiletime.{constValue, erasedValue, summonInline}
import scala.deriving.Mirror
import scala.reflect.TypeTest
import scala.util.boundary
import scala.util.boundary.break

/**
 * A message encoder.
 *
 * @tparam A the type to serialize
 */
trait MessageEncoder[-A]:

  /**
   * Encode a value.
   *
   * @param value the value to encode
   * @return either an error or the result as a list of arguments
   */
  def encode(value: A): Either[ProtocolError, List[String]]

  /**
   * Map the input of this encoder.
   *
   * @param f the function to apply to the input value
   * @tparam B the new type to serialize
   * @return a new encoder 
   */
  def contramap[B](f: B => A): MessageEncoder[B] = value => encode(f(value))

  /**
   * Zip this encoder with another one/
   *
   * @param other the encoder to zip with
   * @tparam B the type to zip with [[A]]
   * @return an encoder taking a tuple `(A, B)` and concatenating the results
   */
  def zip[B](other: => MessageEncoder[B]): MessageEncoder[(A, B)] = (a, b) =>
    for
      resultA <- encode(a)
      resultB <- other.encode(b)
    yield resultA ++ resultB

object MessageEncoder:

  /**
   * Create an always-succeeding encoder.
   *
   * @param value the result of the encoder
   * @tparam A the input type
   * @return a encoder that always succeeds with the passed value
   */
  def succeed[A](value: String): MessageEncoder[A] = _ => Right(List(value))

  /**
   * Create an always-failing encoder.
   *
   * @param error the error of the encoder
   * @return a encoder that always fail with the passed error
   */
  def fail(error: ProtocolError): MessageEncoder[Any] = _ => Left(error)

  /**
   * Derive an encoder from a case class or an enum.
   *
   * @param m the mirror representing the result type
   * @tparam A the result type
   * @return a encoder automatically generated for the type `A`
   */
  inline def derived[A](using m: Mirror.Of[A]): MessageEncoder[A] = inline m match
    case p: Mirror.ProductOf[A & Product] => derivedProduct(p, summonInline[MessageEncoder[p.MirroredElemTypes]]).asInstanceOf[MessageEncoder[A]]
    case s: Mirror.SumOf[A]               => derivedSum(s)

  private inline def genGetProductFields[A <: Product, T <: Tuple](n: Int = 0): A => T = inline erasedValue[T] match
    case _: EmptyTuple => _ => EmptyTuple.asInstanceOf[T]
    case _: (head *: tail) =>
      val getTailFields = genGetProductFields[A, tail](n + 1)
      (value: A) => (value.productElement(n) *: getTailFields(value)).asInstanceOf[T]

  private inline def derivedProduct[A <: Product](m: Mirror.ProductOf[A], decoder: MessageEncoder[m.MirroredElemTypes]): MessageEncoder[A] =
    decoder.contramap(genGetProductFields[A, m.MirroredElemTypes]())

  private inline def genCaseTypeTests[A, T <: Tuple]: List[(TypeTest[A, ? <: A], MessageEncoder[A])] = inline erasedValue[T] match
    case _: EmptyTuple => Nil
    case _: ((nameType, head) *: tail) =>
      val name = MessageName.getMessageNames[head].headOption.getOrElse(constValue[nameType].toString.toLowerCase)
      val test = summonInline[TypeTest[A, head]].asInstanceOf[TypeTest[A, ? <: A]]
      val encoder = derived[head](using summonInline[Mirror.Of[head]]).asInstanceOf[MessageEncoder[A]]

      (test, string.zip(encoder).contramap(value => (name, value))) :: genCaseTypeTests[A, tail]

  private inline def derivedSum[A](m: Mirror.SumOf[A]): MessageEncoder[A] =
    val tests = genCaseTypeTests[A, Tuple.Zip[m.MirroredElemLabels, m.MirroredElemTypes]]
    (value: A) =>
      val encoder =
        tests.collectFirst {
          case (test, encoder) if test.unapply(value).isDefined => encoder
        }.getOrElse(MessageEncoder.fail(ProtocolError.InvalidInput(value.toString, s"No suitable case found")))

      encoder.encode(value)

  private inline def genUnionTypeTests[A, T <: Tuple]: List[(TypeTest[A, ? <: A], MessageEncoder[A])] = inline erasedValue[T] match
    case _: EmptyTuple => Nil
    case _: (head *: tail) =>
      val test = summonInline[TypeTest[A, head]].asInstanceOf[TypeTest[A, ? <: A]]
      val encoder = summonInline[MessageEncoder[head]].asInstanceOf[MessageEncoder[A]]

      (test, encoder) :: genUnionTypeTests[A, tail]

  /**
   * Derive an encoder from an union type.
   *
   * @param m the mirror representing the union type
   * @tparam A the result type (union)
   * @return an encoder automatically generated for the type `A`
   */
  inline given derivedUnion[A](using m: UnionTypeMirror[A]): MessageEncoder[A] =
    val tests = genUnionTypeTests[A, m.ElementTypes]

    new MessageEncoder[A]:
      override def encode(value: A): Either[ProtocolError, List[String]] =
        val encoder =
          tests.collectFirst {
            case (test, encoder) if test.unapply(value).isDefined => encoder
          }.getOrElse(MessageEncoder.fail(ProtocolError.InvalidInput(value.toString, s"No suitable union member found")))

        encoder.encode(value)

  private inline def genUnionTypeTests[A, T <: Tuple]: List[(TypeTest[A, ? <: A], MessageEncoder[A])] = inline erasedValue[T] match
    case _: EmptyTuple => Nil
    case _: (head *: tail) =>
      val test = summonInline[TypeTest[A, head]].asInstanceOf[TypeTest[A, ? <: A]]
      val encoder = summonInline[MessageEncoder[head]].asInstanceOf[MessageEncoder[A]]

      (test, encoder) :: genUnionTypeTests[A, tail]

  inline given derivedUnion[A](using m: UnionTypeMirror[A]): MessageEncoder[A] =
    val tests = genUnionTypeTests[A, m.ElementTypes]

    new MessageEncoder[A]:
      override def encode(value: A): Either[ProtocolError, List[String]] =
        val encoder =
          tests.collectFirst {
            case (test, encoder) if test.unapply(value).isDefined => encoder
          }.getOrElse(MessageEncoder.fail(ProtocolError.InvalidInput(value.toString, s"No suitable union member found")))

        encoder.encode(value)


  inline given ironType[A, C](using encoder: MessageEncoder[A]): MessageEncoder[A :| C] = encoder.asInstanceOf[MessageEncoder[A :| C]]

  inline given newtype[A](using mirror: RefinedTypeOps.Mirror[A]): MessageEncoder[A] =
    summonInline[MessageEncoder[mirror.IronType]].asInstanceOf[MessageEncoder[A]]

  given string: MessageEncoder[String] = value => Right(List(value))

  given int: MessageEncoder[Int] = string.contramap(_.toString)

  given long: MessageEncoder[Long] = string.contramap(_.toString)

  given double: MessageEncoder[Double] = string.contramap(_.toString)

  given boolean: MessageEncoder[Boolean] = string.contramap {
    case true  => "on"
    case false => "off"
  }

  given list[A](using encoder: MessageEncoder[A]): MessageEncoder[List[A]] =
    elements =>
      boundary:
        val buffer = ListBuffer.empty[String]
        for element <- elements do
          encoder.encode(element) match
            case Right(value) => buffer ++= value
            case left         => break(left)

        Right(buffer.toList)

  given emptyTuple: MessageEncoder[EmptyTuple] = _ => Right(Nil)

  given nonEmptyTuple[A, T <: Tuple](using headEncoder: MessageEncoder[A], tailEncoder: MessageEncoder[T]): MessageEncoder[A *: T] = {
    case (head *: tail) =>
      for
        headResult <- headEncoder.encode(head)
        tailResult <- tailEncoder.encode(tail)
      yield headResult ++ tailResult
  }

  given option[A](using encoder: MessageEncoder[A]): MessageEncoder[Option[A]] = {
    case Some(value: A) => encoder.encode(value)
    case None           => Right(Nil)
  }
