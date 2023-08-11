package io.github.projectpidove.showdown.testing.protocol

import io.github.projectpidove.showdown.protocol.*

def assertDecode[T](decoder: MessageDecoder[T], input: MessageInput, expected: T): Unit =
  val result = decoder.decode(input)
  println(result)
  assert(result == Right(expected))

def assertDecodeString[T](decoder: MessageDecoder[T], input: String, expected: T): Unit =
  assertDecode(decoder, MessageInput.fromInput(input), expected)

def assertFail[T](decoder: MessageDecoder[T], input: MessageInput): Unit =
  assert(decoder.decode(input).isLeft)

def assertFailString[T](decoder: MessageDecoder[T], input: String): Unit =
  assertFail(decoder, MessageInput.fromInput(input))
