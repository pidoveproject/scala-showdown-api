package io.github.projectpidove.showdown.testing.protocol

import io.github.projectpidove.showdown.protocol.*
import ProtocolDecoder.given
import utest.*
import zio.prelude.fx.ZPure

object DecodingSuite extends TestSuite:

  def assertDecode[T](decoder: ProtocolDecoder[T], input: ProtocolInput, expected: T): Unit =
    assert(decoder.decode(input) == Right(expected))

  def assertDecodeString[T](decoder: ProtocolDecoder[T], input: String, expected: T): Unit =
    assertDecode(decoder, ProtocolInput.fromInput(input), expected)

  def assertFail[T](decoder: ProtocolDecoder[T], input: ProtocolInput): Unit =
    assert(decoder.decode(input).isLeft)

  def assertFailString[T](decoder: ProtocolDecoder[T], input: String): Unit =
    assertFail(decoder, ProtocolInput.fromInput(input))

  val tests = Tests:

    test("string"):
      test("valid") - assertDecodeString(string, "hello", "hello")
      test("empty") - assertDecodeString(string, "", "")
      test("nothing") - assertFail(string, ProtocolInput.fromList(Nil))

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
      
    