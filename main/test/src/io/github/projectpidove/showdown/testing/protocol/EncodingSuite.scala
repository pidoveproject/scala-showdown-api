package io.github.projectpidove.showdown.testing.protocol

import io.github.projectpidove.showdown.protocol.MessageEncoder
import MessageEncoder.given
import utest.*

object EncodingSuite extends TestSuite:

  case class Person(name: String, age: Int)

  enum Msg:
    case ResetMoney
    case AddMoney(amount: Int)
    case RemoveMoney(amount: Int)
    case SendMoney(amount: Int, to: String)

  val tests = Tests:

    test("string") - assertEncode(string, "hello", List("hello"))
    test("int") - assertEncode(int, 1234, List("1234"))
    test("long") - assertEncode(long, 1234L, List("1234"))
    test("double") - assertEncode(double, 1.234, List("1.234"))
    test("tuple"):
      test("empty") - assertEncode(emptyTuple, EmptyTuple, Nil)
      test("nonEmpty") - assertEncode(nonEmptyTuple[String, (Int, Double)], ("a", 1234, 1.234), List("a", "1234", "1.234"))

    test("derivation"):
      test("product"):
        val encoder = MessageEncoder.derived[Person]
        assertEncode(encoder, Person("Il_totore", 19), List("Il_totore", "19"))

      test("sum"):
        val encoder = MessageEncoder.derived[Msg]

        test("resetMoney") - assertEncode(encoder, Msg.ResetMoney, List("resetmoney"))
        test("addMoney") - assertEncode(encoder, Msg.AddMoney(180), List("addmoney", "180"))
        test("removeMoney") - assertEncode(encoder, Msg.RemoveMoney(40), List("removemoney", "40"))
        test("sendMoney") - assertEncode(encoder, Msg.SendMoney(20, "totore"), List("sendmoney", "20", "totore"))