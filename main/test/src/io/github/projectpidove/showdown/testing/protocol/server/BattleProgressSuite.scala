package io.github.projectpidove.showdown.testing.protocol.server

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.projectpidove.showdown.battle.*
import io.github.projectpidove.showdown.protocol.MessageDecoder
import io.github.projectpidove.showdown.protocol.server.BattleProgressMessage
import io.github.projectpidove.showdown.room.ChatContent
import io.github.projectpidove.showdown.testing.protocol.*
import io.github.projectpidove.showdown.Timestamp
import utest.*

object BattleProgressSuite extends TestSuite:

  val tests = Tests:

    val decoder = summon[MessageDecoder[BattleProgressMessage]]

    test("timerMessage") - assertDecodeString(decoder, "inactive|foo", BattleProgressMessage.TimerMessage(ChatContent("foo")))
    test("timerDisabled") - assertDecodeString(decoder, "inactiveoff|foo", BattleProgressMessage.TimerDisabled(ChatContent("foo")))
    test("turn") - assertDecodeString(decoder, "turn|1", BattleProgressMessage.Turn(TurnNumber(1)))
    test("win") - assertDecodeString(decoder, "t:|1694280223", BattleProgressMessage.Timestamp(Timestamp(1694280223)))