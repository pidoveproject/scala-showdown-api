package io.github.projectpidove.showdown.testing.protocol.server

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.projectpidove.showdown.{Count, FormatName, Generation}
import io.github.projectpidove.showdown.protocol.server.BattleInitializationMessage
import io.github.projectpidove.showdown.battle.*
import io.github.projectpidove.showdown.protocol.MessageDecoder
import io.github.projectpidove.showdown.room.ChatContent
import io.github.projectpidove.showdown.testing.protocol.*
import io.github.projectpidove.showdown.user.{AvatarName, Username}
import utest.*

object BattleInitializationSuite extends TestSuite:

  val tests = Tests:

    val decoder = summon[MessageDecoder[BattleInitializationMessage]]

    test("player") - assertDecodeString(
      decoder,
      "|player|p1|Il_totore|kimonogirl|1500",
      BattleInitializationMessage.Player(PlayerNumber(1), Some(Username("Il_totore")), Some(AvatarName("kimonogirl")), Some(Rating(1500)))
    )

    test("teamsize") - assertDecodeString(decoder, "|teamsize|p1|6", BattleInitializationMessage.TeamSize(PlayerNumber(1), Count(6)))
    test("gameType") - assertDecodeString(decoder, "|gametype|singles", BattleInitializationMessage.GameType(BattleType.Singles()))
    test("gen") - assertDecodeString(decoder, "|gen|9", BattleInitializationMessage.Gen(Generation(9)))
    test("tier") - assertDecodeString(decoder, "|tier|gen9ou", BattleInitializationMessage.Tier(FormatName("gen9ou")))
    test("rated"):
      test("noMessage") - assertDecodeString(decoder, "|rated", BattleInitializationMessage.Rated(None))
      test("withMessage") - assertDecodeString(decoder, "|rated|foo", BattleInitializationMessage.Rated(Some(ChatContent("foo"))))