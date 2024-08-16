package io.github.pidoveproject.showdown.testing.protocol.client

import io.github.iltotore.iron.*
import io.github.pidoveproject.showdown.protocol.MessageEncoder
import io.github.pidoveproject.showdown.protocol.client.*
import io.github.pidoveproject.showdown.testing.protocol.*
import io.github.pidoveproject.showdown.user.Username
import utest.*

object AuthSuite extends TestSuite:

  val tests = Tests:

    val encoder = summon[MessageEncoder[AuthCommand]]

    test("trn") - assertEncode(encoder, AuthCommand.Trn(Username("Il_totore"), 0, "challstr"), List("trn", "Il_totore", "0", "challstr"))
