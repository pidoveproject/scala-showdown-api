package io.github.projectpidove.showdown.testing.protocol.server

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.projectpidove.showdown.{Count, FormatName, Generation, Timestamp}
import io.github.projectpidove.showdown.protocol.server.BattleMessage
import io.github.projectpidove.showdown.battle.*
import io.github.projectpidove.showdown.protocol.MessageDecoder
import io.github.projectpidove.showdown.room.ChatContent
import io.github.projectpidove.showdown.testing.protocol.*
import io.github.projectpidove.showdown.user.{AvatarName, Username}
import utest.*

object BattleSuite extends TestSuite:

  val tests = Tests:

    val decoder = summon[MessageDecoder[BattleMessage]]

    test("player") - assertDecodeString(
      decoder,
      "|player|p1|Il_totore|kimonogirl|1500",
      BattleMessage.Player(PlayerNumber(1), Username("Il_totore"), AvatarName("kimonogirl"), Rating(1500))
    )

    test("teamsize") - assertDecodeString(decoder, "|teamsize|p1|6", BattleMessage.TeamSize(PlayerNumber(1), Count(6)))
    test("gameType") - assertDecodeString(decoder, "|gametype|singles", BattleMessage.GameType(BattleType.Singles()))
    test("gen") - assertDecodeString(decoder, "|gen|9", BattleMessage.Gen(Generation(9)))
    test("tier") - assertDecodeString(decoder, "|tier|gen9ou", BattleMessage.Tier(FormatName("gen9ou")))
    test("rated"):
      test("noMessage") - assertDecodeString(decoder, "|rated", BattleMessage.Rated(None))
      test("withMessage") - assertDecodeString(decoder, "|rated|foo", BattleMessage.Rated(Some(ChatContent("foo"))))
    test("timerMessage") - assertDecodeString(decoder, "inactive|foo", BattleMessage.TimerMessage(ChatContent("foo")))
    test("timerDisabled") - assertDecodeString(decoder, "inactiveoff|foo", BattleMessage.TimerDisabled(ChatContent("foo")))
    test("turn") - assertDecodeString(decoder, "turn|1", BattleMessage.Turn(TurnNumber(1)))
    test("win") - assertDecodeString(decoder, "t:|1694280223", BattleMessage.Timestamp(Timestamp(1694280223)))