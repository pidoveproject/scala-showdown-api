package io.github.pidoveproject.showdown.testing.protocol

import io.github.pidoveproject.showdown.protocol.*

def assertDecode[T](decoder: MessageDecoder[T], input: MessageInput, expected: T): Unit =
  val result = decoder.decode(input)
  assert(
    result == Right(expected),
    s"""Decoding failed.
       |Input: $input
       |Result: $result""".stripMargin
  )

def assertDecodeString[T](decoder: MessageDecoder[T], input: String, expected: T): Unit =
  assertDecode(decoder, MessageInput.fromInput(input), expected)

def assertFail[T](decoder: MessageDecoder[T], input: MessageInput): Unit =
  assert(decoder.decode(input).isLeft)

def assertFailString[T](decoder: MessageDecoder[T], input: String): Unit =
  assertFail(decoder, MessageInput.fromInput(input))

def assertEncode[T](encoder: MessageEncoder[T], input: T, expected: List[String]): Unit =
  val result = encoder.encode(input)
  assert(
    result == Right(expected),
    s"""Expected: $expected
       |Result: $result""".stripMargin
  )
