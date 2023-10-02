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
      BattleMinorActionMessage.Weather(Some(Weather.Snow), None)
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
      BattleMinorActionMessage.SideStart(PlayerId(PlayerNumber(1), Username("Il_totore")), SideFieldEffect("Stealth Rock"))
    )

    test("sideEnd") - assertDecodeString(
      decoder,
      "|-sideend|p1: Il_totore|Stealth Rock",
      BattleMinorActionMessage.SideEnd(PlayerId(PlayerNumber(1), Username("Il_totore")), SideFieldEffect("Stealth Rock"))
    )

    test("item") - assertDecodeString(
      decoder,
      "|-item|p1a: Heatran|leftovers",
      BattleMinorActionMessage.Item(ActiveId(pos"p1a", Surname("Heatran")), ItemName("leftovers"), None)
    )

    test("endItem") - assertDecodeString(
      decoder,
      "|-enditem|p1a: Heatran|leftovers|[from] move: Knock Off",
      BattleMinorActionMessage.EndItem(ActiveId(pos"p1a", Surname("Heatran")), ItemName("leftovers"), Some(Effect.Move(MoveName("Knock Off"))))
    )

    test("ability") - assertDecodeString(
      decoder,
      "|-ability|p1a: Gardevoir|Fire Flash|[from] ability: Trace",
      BattleMinorActionMessage.Ability(ActiveId(pos"p1a", Surname("Gardevoir")), AbilityName("Fire Flash"), Some(Effect.Ability(AbilityName("Trace"))))
    )

    test("endAbility") - assertDecodeString(
      decoder,
      "|-endability|p1a: Gardevoir",
      BattleMinorActionMessage.EndAbility(ActiveId(pos"p1a", Surname("Gardevoir")))
    )

    test("transform") - assertDecodeString(
      decoder,
      "|-transform|p1a: Ditto|p2a: Skarmory|[from] ability: Imposter",
      BattleMinorActionMessage.Transform(
        ActiveId(pos"p1a", Surname("Ditto")),
        ActiveId(pos"p2a", Surname("Skarmory")),
        Some(Effect.Ability(AbilityName("Imposter")))
      )
    )

    test("mega") - assertDecodeString(
      decoder,
      "|-mega|p1a: Beedrill|Beedrillite",
      BattleMinorActionMessage.Mega(ActiveId(pos"p1a", Surname("Beedrill")), ItemName("Beedrillite"))
    )

    test("ultraBurst") - assertDecodeString(
      decoder,
      "|-burst|p1a: Necrozma|Ultra-Necrozma|Ultranecrozium-Z",
      BattleMinorActionMessage.UltraBurst(
        pokemon = ActiveId(pos"p1a", Surname("Necrozma")),
        species = SpeciesName("Ultra-Necrozma"),
        item = ItemName("Ultranecrozium-Z")
      )
    )

    test("zpower") - assertDecodeString(
      decoder,
      "|-zpower|p1a: Durant",
      BattleMinorActionMessage.ZPower(ActiveId(pos"p1a", Surname("Durant")))
    )

    test("zbroken") - assertDecodeString(
      decoder,
      "|-zpower|p1a: Durant",
      BattleMinorActionMessage.ZPower(ActiveId(pos"p1a", Surname("Durant")))
    )

    test("activate") - assertDecodeString(
      decoder,
      "|-activate|[from] move: Future Sight",
      BattleMinorActionMessage.Activate(Effect.Move(MoveName("Future Sight")))
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