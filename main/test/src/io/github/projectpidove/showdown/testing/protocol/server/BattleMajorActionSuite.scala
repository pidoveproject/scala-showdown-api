package io.github.projectpidove.showdown.testing.protocol.server

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.projectpidove.showdown.battle.*
import io.github.projectpidove.showdown.battle.PokemonPosition.pos
import io.github.projectpidove.showdown.protocol.MessageDecoder
import io.github.projectpidove.showdown.protocol.server.BattleMajorActionMessage
import io.github.projectpidove.showdown.testing.protocol.*
import io.github.projectpidove.showdown.team.*
import utest.*

object BattleMajorActionSuite:
  
  val tests = Tests:
    
    val decoder = summon[MessageDecoder[BattleMajorActionMessage]]

    test("move") - assertDecodeString(
      decoder,
      "|move|p1a: Qwilfish|Hydro Pump|p2a: Sudowoodo",
      BattleMajorActionMessage.Move(
        pokemon = PokemonId(pos"p1a", Surname("Qwilfish")),
        move = MoveName("Hydro Pump"),
        target = PokemonId(pos"p2a", Surname("Sudowoodo"))
      )
    )

    test("switch") - assertDecodeString(
      decoder,
      "|switch|p1a: Qwilfish|Qwilfish, L84, M|55/100 psn",
      BattleMajorActionMessage.Switch(
        pokemon = PokemonId(pos"p1a", Surname("Qwilfish")),
        details = PokemonDetails(
          species = SpeciesName("Qwilfish"),
          level = Some(Level(84)),
          gender = Some(Gender.Male)
        ),
        healthStatus = HealthStatus(Health.percent(55), Some(StatusEffect.Poison))
      )
    )

    test("detailsChange") - assertDecodeString(
      decoder,
      "|detailschange|p2a: Sableye|Sableye-Mega, M",
      BattleMajorActionMessage.DetailsChange(
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
      BattleMajorActionMessage.Replace(
        pokemon = PokemonId(pos"p2a", Surname("Zoroark")),
        details = PokemonDetails(
          species = SpeciesName("Zoroark"),
          gender = Some(Gender.Male)
        ),
        healthStatus = HealthStatus(Health.percent(70))
      )
    )

    test("swap") - assertDecodeString(
      decoder,
      "|swap|p1a: Blissey|b",
      BattleMajorActionMessage.Swap(
        pokemon = PokemonId(pos"p1a", Surname("Blissey")),
        slot = PokemonSlot(1)
      )
    )

    test("unable") - assertDecodeString(
      decoder,
      "|cant|p2a: Sudowoodo|flinch",
      BattleMajorActionMessage.Unable(
        pokemon = PokemonId(pos"p2a", Surname("Sudowoodo")),
        reason = "flinch",
        move = None
      )
    )

    test("faint") - assertDecodeString(
      decoder,
      "faint|p2a: Sudowoodo",
      BattleMajorActionMessage.Faint(PokemonId(pos"p2a", Surname("Sudowoodo")))
    )