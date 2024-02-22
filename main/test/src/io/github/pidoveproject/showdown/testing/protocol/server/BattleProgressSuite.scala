package io.github.pidoveproject.showdown.testing.protocol.server

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.pidoveproject.showdown.battle.*
import io.github.pidoveproject.showdown.battle.ActivePosition.pos
import io.github.pidoveproject.showdown.protocol.MessageDecoder
import io.github.pidoveproject.showdown.protocol.server.BattleProgressMessage
import io.github.pidoveproject.showdown.room.ChatContent
import io.github.pidoveproject.showdown.testing.protocol.*
import io.github.pidoveproject.showdown.user.Username
import io.github.pidoveproject.showdown.Timestamp
import io.github.pidoveproject.showdown.protocol.server.choice.{ActiveChoice, ChoiceError, ChoiceRequest, MoveChoice, MoveRange, PokemonChoice, TeamChoice}
import io.github.pidoveproject.showdown.team.*
import utest.*

import scala.io.Source
import scala.util.Using

object BattleProgressSuite extends TestSuite:

  val tests = Tests:

    val decoder = summon[MessageDecoder[BattleProgressMessage]]

    test("timerMessage") - assertDecodeString(decoder, "|inactive|foo", BattleProgressMessage.TimerMessage(ChatContent("foo")))
    test("timerDisabled") - assertDecodeString(decoder, "|inactiveoff|foo", BattleProgressMessage.TimerDisabled(ChatContent("foo")))
    test("turn") - assertDecodeString(decoder, "|turn|1", BattleProgressMessage.Turn(TurnNumber(1)))
    test("win") - assertDecodeString(decoder, "|win|Il_totore", BattleProgressMessage.Win(Username("Il_totore")))
    test("timestamp") - assertDecodeString(decoder, "|t:|1694280223", BattleProgressMessage.Timestamp(Timestamp(1694280223)))
    test("error"):
      test("invalid") - assertDecodeString(
        decoder,
        "|error|[Invalid choice] foo",
        BattleProgressMessage.Error(ChoiceError.Invalid(ChatContent("foo")))
      )

      test("unavailable") - assertDecodeString(
        decoder,
        "|error|[Unavailable choice] bar",
        BattleProgressMessage.Error(ChoiceError.Unavailable(ChatContent("bar")))
      )

    test("request"):
      Using.resource(Source.fromFile("main/test/resources/choice_request.json")): source =>
        val request = source.getLines().mkString
        val result =
          ChoiceRequest(
            active = List(ActiveChoice(
              moves = List(
                MoveChoice(
                  name = MoveName("Light Screen"),
                  id = "lightscreen",
                  pp = PP(48),
                  maxPP = PP(48),
                  range = MoveRange.AllySide,
                  disabled = false
                ),
                MoveChoice(
                  name = MoveName("U-turn"),
                  id = "uturn",
                  pp = PP(32),
                  maxPP = PP(32),
                  range = MoveRange.Normal,
                  disabled = false
                )
              )
            )),
            team = TeamChoice(
              name = Username("Zarel"),
              player = PlayerNumber(2),
              pokemon = List(
                PokemonChoice(
                  id = TeamId(PlayerNumber(2), Surname("Ledian")),
                  details = PokemonDetails(
                    species = SpeciesName("Ledian"),
                    level = Some(Level(83)),
                    gender = Some(Gender.Male)
                  ),
                  condition = Condition(Health(227, 227)),
                  active = true,
                  stats = Map(
                    StatType.Attack -> Stat(106),
                    StatType.Defense -> Stat(131),
                    StatType.SpecialAttack -> Stat(139),
                    StatType.SpecialDefense -> Stat(230),
                    StatType.Speed -> Stat(189)
                  ),
                  moves = List(
                    MoveName("lightscreen"),
                    MoveName("uturn")
                  ).assume,
                  item = Some(ItemName("leftovers")),
                  pokeball = "pokeball",
                  baseAbility = AbilityName("swarm"),
                  ability = AbilityName("swarm")
                ),
                PokemonChoice(
                  id = TeamId(PlayerNumber(2), Surname("Pyukumuku")),
                  details = PokemonDetails(
                    species = SpeciesName("Pyukumuku"),
                    level = Some(Level(83)),
                    gender = Some(Gender.Female)
                  ),
                  condition = Condition(Health(227, 227)),
                  active = false,
                  stats = Map(
                    StatType.Attack -> Stat(104),
                    StatType.Defense -> Stat(263),
                    StatType.SpecialAttack -> Stat(97),
                    StatType.SpecialDefense -> Stat(263),
                    StatType.Speed -> Stat(56)
                  ),
                  moves = List(
                    MoveName("recover"),
                    MoveName("counter"),
                    MoveName("lightscreen"),
                    MoveName("reflect")
                  ).assume,
                  item = Some(ItemName("lightclay")),
                  pokeball = "pokeball",
                  baseAbility = AbilityName("innardsout"),
                  ability = AbilityName("innardsout")
                )
              )
            ),
            requestId = Some(3)
          )

        assertDecodeString(
          decoder,
          s"|request|$request",
          BattleProgressMessage.Request(Some(result))
        )