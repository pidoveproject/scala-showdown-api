package io.github.projectpidove.showdown.testing.protocol.server

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.projectpidove.showdown.battle.*
import io.github.projectpidove.showdown.battle.PokemonPosition.pos
import io.github.projectpidove.showdown.protocol.MessageDecoder
import io.github.projectpidove.showdown.protocol.server.BattleProgressMessage
import io.github.projectpidove.showdown.room.ChatContent
import io.github.projectpidove.showdown.testing.protocol.*
import io.github.projectpidove.showdown.user.Username
import io.github.projectpidove.showdown.Timestamp
import io.github.projectpidove.showdown.team.*
import utest.*

object BattleProgressSuite extends TestSuite:

  val tests = Tests:

    val decoder = summon[MessageDecoder[BattleProgressMessage]]

    test("timerMessage") - assertDecodeString(decoder, "|inactive|foo", BattleProgressMessage.TimerMessage(ChatContent("foo")))
    test("timerDisabled") - assertDecodeString(decoder, "|inactiveoff|foo", BattleProgressMessage.TimerDisabled(ChatContent("foo")))
    test("turn") - assertDecodeString(decoder, "|turn|1", BattleProgressMessage.Turn(TurnNumber(1)))
    test("win") - assertDecodeString(decoder, "|win|Il_totore", BattleProgressMessage.Win(Username("Il_totore")))
    test("timestamp") - assertDecodeString(decoder, "|t:|1694280223", BattleProgressMessage.Timestamp(Timestamp(1694280223)))
    test("move") - assertDecodeString(
      decoder,
      "|move|p1a: Qwilfish|Hydro Pump|p2a: Sudowoodo",
      BattleProgressMessage.Move(
        pokemon = PokemonId(pos"p1a", Surname("Qwilfish")),
        move = MoveName("Hydro Pump"),
        target = PokemonId(pos"p2a", Surname("Sudowoodo"))
      )
    )

    test("switch") - assertDecodeString(
      decoder,
      "|switch|p1a: Qwilfish|Qwilfish, L84, M|55/100 psn",
      BattleProgressMessage.Switch(
        pokemon = PokemonId(pos"p1a", Surname("Qwilfish")),
        details = PokemonDetails(
          species = SpeciesName("Qwilfish"),
          level = Some(Level(84)),
          gender = Some(Gender.Male)
        ),
        healthStatus = HealthStatus(Health(55), Some(Status.Poison))
      )
    )

    test("detailsChange") - assertDecodeString(
      decoder,
      "|detailschange|p2a: Sableye|Sableye-Mega, M",
      BattleProgressMessage.DetailsChange(
        pokemon = PokemonId(pos"p2a", Surname("Sableye")),
        details = PokemonDetails(
          species = SpeciesName("Sableye-Mega"),
          gender = Some(Gender.Male)
        ),
        healthStatus = None
      )
    )

    test("replace") - assertDecodeString(
      decoder,
      "|replace|p2a: Zoroark|Zoroark, M|70/100",
      BattleProgressMessage.Replace(
        pokemon = PokemonId(pos"p2a", Surname("Zoroark")),
        details = PokemonDetails(
          species = SpeciesName("Zoroark"),
          gender = Some(Gender.Male)
        ),
        healthStatus = HealthStatus(Health(70))
      )
    )

    test("swap") - assertDecodeString(
      decoder,
      "|swap|p1a: Blissey|b",
      BattleProgressMessage.Swap(
        pokemon = PokemonId(pos"p1a", Surname("Blissey")),
        slot = PokemonSlot(1)
      )
    )

    test("unable") - assertDecodeString(
      decoder,
      "|cant|p2a: Sudowoodo|flinch",
      BattleProgressMessage.Unable(
        pokemon = PokemonId(pos"p2a", Surname("Sudowoodo")),
        reason = "flinch",
        move = None
      )
    )

    test("faint") - assertDecodeString(
      decoder,
      "faint|p2a: Sudowoodo",
      BattleProgressMessage.Faint(PokemonId(pos"p2a", Surname("Sudowoodo")))
    )