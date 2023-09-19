package io.github.projectpidove.showdown.testing.protocol.server

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.projectpidove.showdown.Count
import io.github.projectpidove.showdown.battle.*
import io.github.projectpidove.showdown.battle.PokemonPosition.pos
import io.github.projectpidove.showdown.protocol.MessageDecoder
import io.github.projectpidove.showdown.protocol.server.BattleMinorActionMessage
import io.github.projectpidove.showdown.room.ChatContent
import io.github.projectpidove.showdown.testing.protocol.*
import io.github.projectpidove.showdown.team.*
import io.github.projectpidove.showdown.user.Username
import utest.*

object BattleMinorActionSuite extends TestSuite:
  
  val tests = Tests:
    
    val decoder = summon[MessageDecoder[BattleMinorActionMessage]]

    test("weather") - assertDecodeString(
      decoder,
      "|-weather|snow",
      BattleMinorActionMessage.Weather(Some(Weather.Snow))
    )

    test("fieldStart") - assertDecodeString(
      decoder,
      "|-fieldstart|electric terrain",
      BattleMinorActionMessage.FieldStart(FieldEffect("electric terrain"))
    )

    test("fieldEnd") - assertDecodeString(
      decoder,
      "|-fieldend|electric terrain",
      BattleMinorActionMessage.FieldEnd(FieldEffect("electric terrain"))
    )

    test("sideStart") - assertDecodeString(
      decoder,
      "|-sidestart|p1: Il_totore|Stealth Rock",
      BattleMinorActionMessage.SideStart(PlayerPosition(PlayerNumber(1), Username("Il_totore")), SideFieldEffect("Stealth Rock"))
    )

    test("sideEnd") - assertDecodeString(
      decoder,
      "|-sideend|p1: Il_totore|Stealth Rock",
      BattleMinorActionMessage.SideEnd(PlayerPosition(PlayerNumber(1), Username("Il_totore")), SideFieldEffect("Stealth Rock"))
    )

    test("item") - assertDecodeString(
      decoder,
      "|-item|p1a: Heatran|leftovers",
      BattleMinorActionMessage.Item(PokemonId(pos"p1a", Surname("Heatran")), ItemName("leftovers"), None)
    )

    test("endItem") - assertDecodeString(
      decoder,
      "|-enditem|p1a: Heatran|leftovers|move: Knock Off",
      BattleMinorActionMessage.EndItem(PokemonId(pos"p1a", Surname("Heatran")), ItemName("leftovers"), Some(Effect("move: Knock Off")))
    )

    test("ability") - assertDecodeString(
      decoder,
      "|-ability|p1a: Gardevoir|Fire Flash|ability: Trace",
      BattleMinorActionMessage.Ability(PokemonId(pos"p1a", Surname("Gardevoir")), AbilityName("Fire Flash"), Some(Effect("ability: Trace")))
    )

    test("endAbility") - assertDecodeString(
      decoder,
      "|-endability|p1a: Gardevoir",
      BattleMinorActionMessage.EndAbility(PokemonId(pos"p1a", Surname("Gardevoir")))
    )

    test("transform") - assertDecodeString(
      decoder,
      "|-transform|p1a: Ditto|Skarmory",
      BattleMinorActionMessage.Transform(PokemonId(pos"p1a", Surname("Ditto")), SpeciesName("Skarmory"))
    )

    test("mega") - assertDecodeString(
      decoder,
      "|-mega|p1a: Beedrill|Beedrillite",
      BattleMinorActionMessage.Mega(PokemonId(pos"p1a", Surname("Beedrill")), ItemName("Beedrillite"))
    )

    test("ultraBurst") - assertDecodeString(
      decoder,
      "|-burst|p1a: Necrozma|Ultra-Necrozma|Ultranecrozium-Z",
      BattleMinorActionMessage.UltraBurst(
        pokemon = PokemonId(pos"p1a", Surname("Necrozma")),
        species = SpeciesName("Ultra-Necrozma"),
        item = ItemName("Ultranecrozium-Z")
      )
    )

    test("zpower") - assertDecodeString(
      decoder,
      "|-zpower|p1a: Durant",
      BattleMinorActionMessage.ZPower(PokemonId(pos"p1a", Surname("Durant")))
    )

    test("zbroken") - assertDecodeString(
      decoder,
      "|-zpower|p1a: Durant",
      BattleMinorActionMessage.ZPower(PokemonId(pos"p1a", Surname("Durant")))
    )

    test("activate") - assertDecodeString(
      decoder,
      "|-activate|Future Sight",
      BattleMinorActionMessage.Activate(Effect("Future Sight"))
    )

    test("hint") - assertDecodeString(
      decoder,
      "|-hint|foo",
      BattleMinorActionMessage.Hint(ChatContent("foo"))
    )

    test("message") - assertDecodeString(
      decoder,
      "|-message|foo",
      BattleMinorActionMessage.Message(ChatContent("foo"))
    )