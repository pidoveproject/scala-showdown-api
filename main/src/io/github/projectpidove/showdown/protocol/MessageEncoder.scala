package io.github.projectpidove.showdown.protocol

import scala.compiletime.{constValue, erasedValue, summonInline}
import scala.deriving.Mirror
import scala.reflect.TypeTest

trait MessageEncoder[-A]:

  def encode(value: A): Either[ProtocolError, List[String]]

  def contramap[B](f: B => A): MessageEncoder[B] = value => encode(f(value))

  def zip[B](other: => MessageEncoder[B]): MessageEncoder[(A, B)] = (a, b) =>
    for
      resultA <- encode(a)
      resultB <- other.encode(b)
    yield
      resultA ++ resultB

object MessageEncoder:

  def succeed[A](value: String): MessageEncoder[A] = _ => Right(List(value))

  def fail(error: ProtocolError): MessageEncoder[Any] = _ => Left(error)

  inline def derived[A](using m: Mirror.Of[A]): MessageEncoder[A] = inline m match
    case p: Mirror.ProductOf[A & Product] => derivedProduct(p, summonInline[MessageEncoder[p.MirroredElemTypes]]).asInstanceOf[MessageEncoder[A]]
    case s: Mirror.SumOf[A] => derivedSum(s)

  private inline def genGetProductFields[A <: Product, T <: Tuple](n: Int = 0): A => T = inline erasedValue[T] match
    case _: EmptyTuple => _ => EmptyTuple.asInstanceOf[T]
    case _: (head *: tail) =>
      val getTailFields = genGetProductFields[A, tail](n+1)
      (value: A) => (value.productElement(n) *: getTailFields(value)).asInstanceOf[T]

  private inline def derivedProduct[A <: Product](m: Mirror.ProductOf[A], decoder: MessageEncoder[m.MirroredElemTypes]): MessageEncoder[A] =
    decoder.contramap(genGetProductFields[A, m.MirroredElemTypes]())

  private inline def genTypeTests[A, T <: Tuple]: List[(TypeTest[A, ? <: A], MessageEncoder[A])] = inline erasedValue[T] match
    case _: EmptyTuple => Nil
    case _: ((nameType, head) *: tail) =>
      val name = MessageName.getMessageNames[head].headOption.getOrElse(constValue[nameType].toString.toLowerCase)
      val test = summonInline[TypeTest[A, head]].asInstanceOf[TypeTest[A, ? <: A]]
      val encoder = derived[head](using summonInline[Mirror.Of[head]]).asInstanceOf[MessageEncoder[A]]

      (test, string.zip(encoder).contramap(value => (name, value))) :: genTypeTests[A, tail]

  private inline def derivedSum[A](m: Mirror.SumOf[A]): MessageEncoder[A] =
    val tests = genTypeTests[A, Tuple.Zip[m.MirroredElemLabels, m.MirroredElemTypes]]
    (value: A) =>
      val encoder =
        tests.collectFirst {
          case (test, encoder) if test.unapply(value).isDefined => encoder
        }.getOrElse(MessageEncoder.fail(ProtocolError.InvalidInput(value.toString, s"No suitable case found")))

      encoder.encode(value)

  given string: MessageEncoder[String] = value => Right(List(value))

  given int: MessageEncoder[Int] = string.contramap(_.toString)

  given long: MessageEncoder[Long] = string.contramap(_.toString)

  given double: MessageEncoder[Double] = string.contramap(_.toString)

  given emptyTuple: MessageEncoder[EmptyTuple] = _ => Right(Nil)

  given nonEmptyTuple[A, T <: Tuple](using headEncoder: MessageEncoder[A], tailEncoder: MessageEncoder[T]): MessageEncoder[A *: T] = {
    case (head *: tail) =>
      for
        headResult <- headEncoder.encode(head)
        tailResult <- tailEncoder.encode(tail)
      yield
        headResult ++ tailResult
  }