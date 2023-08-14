package io.github.projectpidove.showdown.testing.protocol.server

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.projectpidove.showdown.room.RoomId
import io.github.projectpidove.showdown.*
import io.github.projectpidove.showdown.Count
import io.github.projectpidove.showdown.protocol.{MessageDecoder, MessageInput}
import io.github.projectpidove.showdown.protocol.server.TournamentMessage
import io.github.projectpidove.showdown.protocol.server.tournament.*
import io.github.projectpidove.showdown.testing.protocol.assertDecodeString
import io.github.projectpidove.showdown.user.Username
import utest.*
import io.github.iltotore.iron.zioJson.given
import zio.json.JsonDecoder

object TournamentSuite extends TestSuite:
  val tests = Tests:

    test("tournamentGenerator"):

      val decoder = summon[MessageDecoder[TournamentGenerator]]

      test("roundRobin"):
        test("noPrefix") - assertDecodeString(decoder, "Round Robin", TournamentGenerator.RoundRobin(Count(1)))
        test("prefixed") - assertDecodeString(decoder, "Double Round Robin", TournamentGenerator.RoundRobin(Count(2)))

      test("elimination") - assertDecodeString(decoder, "Single Elimination", TournamentGenerator.Elimination(Count(1)))

    val decoder = summon[MessageDecoder[TournamentMessage]]

    test("create") - assertDecodeString(decoder, "|tournament|create|gen9monotype|Single Elimination|128", TournamentMessage.Create(
      FormatName("gen9monotype"),
      TournamentGenerator.Elimination(Count(1)),
      Count(128),
    ))
    test("update"):
      test("empty") - assertDecodeString(decoder, "|tournament|update|{}", TournamentMessage.Update(TournamentUpdate()))
      test("users") - assertDecodeString(decoder, """|tournament|update|{"bracketData":{"type":"Tree","rootNode":null,"users":["KeshBa45","Leeko","Olekpop1","we up"]}}""", TournamentMessage.Update(TournamentUpdate(
        bracketData = Some(BracketData(
          bracketType = BracketType.Tree,
          users = Some(List(Username("KeshBa45"), Username("Leeko"), Username("Olekpop1"), Username("we up")))
        ))
      )))
      test("settings") - assertDecodeString(decoder, """|tournament|update|{"format":"gen9monotype","generator":"Single Elimination","playerCap":128,"isStarted":false,"isJoined":false}""".stripMargin, TournamentMessage.Update(TournamentUpdate(
        format = Some(FormatName("gen9monotype")),
        generator= Some(TournamentGenerator.Elimination(Count(1))),
        playerCap = Some(Count(128)),
        isStarted = Some(false),
        isJoined = Some(false),
      )))
      test("ingame") - assertDecodeString(decoder, """|tournament|update|{"bracketData":{"type":"tree","rootNode":{"children":[{"children":[{"team":"Karan Goyal#2432"},{"team":"Ray22-1"}],"state":"available"},{"children":[{"team":"Zarel"},{"team":"Il_totore"}],"state":"inprogress"}],"state":"unavailable"}}}""", TournamentMessage.Update(TournamentUpdate(
        bracketData = Some(BracketData(
          bracketType = BracketType.Tree,
          rootNode = Some(BracketNode.Node(
            children = List(
              BracketNode.Node(
                children = List(
                  BracketNode.Leaf("Karan Goyal#2432"),
                  BracketNode.Leaf("Ray22-1")
                ),
                state = BattleState.Available
              ),
              BracketNode.Node(
                children = List(
                  BracketNode.Leaf("Zarel"),
                  BracketNode.Leaf("Il_totore")
                ),
                state = BattleState.InProgress
              )
            ),
            state = BattleState.Unavailable,
          ))
        ))
      )))

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
    test("end") - assertDecodeString(decoder, """|tournament|end|{"results":[["trichotomy"]],"format":"gen9monotype","generator":"Single Elimination","bracketData":{"type":"tree","rootNode":{"children":[{"children":[{"team":"Karan Goyal#2432"},{"team":"Ray22-1"}],"state":"available"},{"children":[{"team":"Zarel"},{"team":"Il_totore"}],"state":"unavailable"}],"state":"unavailable"}}}""", TournamentMessage.End(TournamentEnd(
      List(List(Username("trichotomy"))),
      FormatName("gen9monotype"),
      TournamentGenerator.Elimination(Count(1)),
      BracketData(
        bracketType = BracketType.Tree,
        rootNode = Some(BracketNode.Node(
          children = List(
            BracketNode.Node(
              children = List(
                BracketNode.Leaf("Karan Goyal#2432"),
                BracketNode.Leaf("Ray22-1")
              ),
              state = BattleState.Available
            ),
            BracketNode.Node(
              children = List(
                BracketNode.Leaf("Zarel"),
                BracketNode.Leaf("Il_totore")
              ),
              state = BattleState.Unavailable
            )
          ),
          state = BattleState.Unavailable)))
    )))
    test("scouting") - assertDecodeString(decoder, "|tournament|scouting|allow", TournamentMessage.Scouting(TournamentSetting.Allow()))
    test("autoStart") :
      test("on") - assertDecodeString(decoder, "|tournament|autostart|on|1000", TournamentMessage.AutoStart(TournamentAutoStart.On(Timestamp(1000))))
      test("off") - assertDecodeString(decoder, "|tournament|autostart|off", TournamentMessage.AutoStart(TournamentAutoStart.Off()))

    test("autoDq") :
      test("on") - assertDecodeString(decoder, "|tournament|autodq|on|1000", TournamentMessage.AutoDq(TournamentAutoDq.On(Timestamp(1000))))
      test("off") - assertDecodeString(decoder, "|tournament|autodq|off", TournamentMessage.AutoDq(TournamentAutoDq.Off()))
      test("target") - assertDecodeString(decoder, "|tournament|autodq|target|1000", TournamentMessage.AutoDq(TournamentAutoDq.Target(Timestamp(1000))))