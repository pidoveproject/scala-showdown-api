package io.github.projectpidove.showdown.testing.protocol.server

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.projectpidove.showdown.room.RoomId
import io.github.projectpidove.showdown.*
import io.github.projectpidove.showdown.Count
import io.github.projectpidove.showdown.protocol.MessageDecoder
import io.github.projectpidove.showdown.protocol.server.TournamentMessage
import io.github.projectpidove.showdown.protocol.server.tournament.*
import io.github.projectpidove.showdown.testing.protocol.assertDecodeString
import io.github.projectpidove.showdown.user.Username
import utest.*

object TournamentSuite extends TestSuite:
  val tests = Tests:

    val decoder = summon[MessageDecoder[TournamentMessage]]

    test("updateEnd") - assertDecodeString(decoder, "|tournament|updateEnd", TournamentMessage.UpdateEnd())
    test("error") - assertDecodeString(decoder, "|tournament|error|ERROR", TournamentMessage.Error("ERROR"))
    test("forceEnd") - assertDecodeString(decoder, "|tournament|forceend", TournamentMessage.ForceEnd())
    test("join") - assertDecodeString(decoder, "|tournament|join|UnderSkyle", TournamentMessage.Join(Username("UnderSkyle")))
    test("leave") - assertDecodeString(decoder, "|tournament|leave|UnderSkyle", TournamentMessage.Leave(Username("UnderSkyle")))
    test("replace") - assertDecodeString(decoder, "|tournament|replace|UnderSkyle|Iltotore", TournamentMessage.Replace(
      Username("UnderSkyle"),
      Username("Iltotore"))
    )
    test("start") - assertDecodeString(decoder, "|tournament|start|5", TournamentMessage.Start(Count(5)))
    test("disqualify") - assertDecodeString(decoder, "|tournament|disqualify|UnderSkyle", TournamentMessage.Disqualify(Username("UnderSkyle")))
    test("battleStart") - assertDecodeString(decoder, "|tournament|battlestart|UnderSkyle|Iltotore|battle-gen7randombattle-1919195988", TournamentMessage.BattleStart(
      Username("UnderSkyle"),
      Username("Iltotore"),
      RoomId("battle-gen7randombattle-1919195988"))
    )
    test("battleEnd") - assertDecodeString(decoder, "|tournament|battleend|P4bl00|angeleri|loss|0,1|success|battle-gen9doublesou-1919196310", TournamentMessage.BattleEnd(
      Username("P4bl00"),
      Username("angeleri"),
      BattleResult.Loss(),
      BattleScore(Score(0),Score(1)),
      TournamentRecord.Success(),
      RoomId("battle-gen9doublesou-1919196310")
    ))
    test("scouting") - assertDecodeString(decoder, "|tournament|scouting|allow", TournamentMessage.Scouting(TournamentSetting.Allow()))
