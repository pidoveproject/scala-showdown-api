package io.github.projectpidove.showdown.testing.protocol

import io.github.projectpidove.showdown.protocol.*
import MessageDecoder.given
import utest.*
import zio.prelude.fx.ZPure

object DecodingSuite extends TestSuite:

  def assertDecode[T](decoder: MessageDecoder[T], input: MessageInput, expected: T): Unit =
    assert(decoder.decode(input) == Right(expected))

  def assertDecodeString[T](decoder: MessageDecoder[T], input: String, expected: T): Unit =
    assertDecode(decoder, MessageInput.fromInput(input), expected)

  def assertFail[T](decoder: MessageDecoder[T], input: MessageInput): Unit =
    assert(decoder.decode(input).isLeft)

  def assertFailString[T](decoder: MessageDecoder[T], input: String): Unit =
    assertFail(decoder, MessageInput.fromInput(input))

  case class Person(name: String, age: Int)

  enum Msg:
    case ResetMoney
    case AddMoney(amount: Int)
    case RemoveMoney(amount: Int)
    case SendMoney(amount: Int, to: String)

  val tests = Tests:

    test("string"):
      test("valid") - assertDecodeString(string, "hello", "hello")
      test("empty") - assertDecodeString(string, "", "")
      test("nothing") - assertFail(string, MessageInput.fromList(Nil))

    test("int"):
      test("valid") - assertDecodeString(int, "1234", 1234)
      test("float") - assertFailString(int, "1.234")
      test("string") - assertFailString(int, "abcd")

    test("long"):
      test("valid") - assertDecodeString(long, "1234", 1234L)
      test("float") - assertFailString(long, "1.234")
      test("string") - assertFailString(long, "abcd")

    test("double"):
      test("int") - assertDecodeString(double, "1234", 1234.0)
      test("float") - assertDecodeString(double, "1.234", 1.234)
      test("string") - assertFailString(double, "abcd")

    test("tuple"):
      test("empty") - assertDecodeString(emptyTuple, "", EmptyTuple)
      test("nonEmpty") - assertDecodeString(nonEmptyTuple[String, (Int, Double)], "abcd|1234|1.234", ("abcd", 1234, 1.234))
      
    test("derivation"):
      test("product"):
        val decoder = MessageDecoder.derived[Person]

        test("valid") - assertDecodeString(decoder, "totore|19", Person("totore", 19))
        test("invalidValue") - assertFailString(decoder, "totore|abcd")
        test("missingValue") - assertFailString(decoder, "totore")

      test("sum"):
        val decoder = MessageDecoder.derived[Msg]

        test("resetMoney") - assertDecodeString(decoder, "resetmoney", Msg.ResetMoney)
        test("addMoney") - assertDecodeString(decoder, "addmoney|180", Msg.AddMoney(180))
        test("removeMoney") - assertDecodeString(decoder, "removemoney|40", Msg.RemoveMoney(40))
        test("sendMoney") - assertDecodeString(decoder, "sendmoney|20|totore", Msg.SendMoney(20, "totore"))