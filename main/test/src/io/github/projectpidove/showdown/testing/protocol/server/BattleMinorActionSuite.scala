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

    test("fail") - assertDecodeString(
      decoder,
      "|-fail|p1a: Froslass|Destiny Bound",
      BattleMinorActionMessage.Fail(PokemonId(pos"p1a", Surname("Froslass")), MoveName("Destiny Bound"))
    )

    test("block") - assertDecodeString(
      decoder,
      "|-block|p1a: Celesteela|Protect|p2a: Nidoking",
      BattleMinorActionMessage.Block(
        pokemon = PokemonId(pos"p1a", Surname("Celesteela")),
        effect = Effect("Protect"),
        attacker = PokemonId(pos"p2a", Surname("Nidoking"))
      )
    )

    test("noTarget") - assertDecodeString(
      decoder,
      "|-notarget|p1a: Girafarig",
      BattleMinorActionMessage.NoTarget(PokemonId(pos"p1a", Surname("Girafarig")))
    )

    test("miss") - assertDecodeString(
      decoder,
      "|-miss|p1a: Gholdengo|p2a: Garganacl",
      BattleMinorActionMessage.Miss(PokemonId(pos"p1a", Surname("Gholdengo")), PokemonId(pos"p2a", Surname("Garganacl")))
    )

    test("damage") - assertDecodeString(
      decoder,
      "|-damage|p1a: Gholdengo|12/100",
      BattleMinorActionMessage.Damage(PokemonId(pos"p1a", Surname("Gholdengo")), HealthStatus(Health.percent(12)))
    )

    test("heal") - assertDecodeString(
      decoder,
      "|-heal|p1a: Gholdengo|6/100",
      BattleMinorActionMessage.Heal(PokemonId(pos"p1a", Surname("Gholdengo")), HealthStatus(Health.percent(6)))
    )

    test("setHealth") - assertDecodeString(
      decoder,
      "|-sethp|p1a: Azumarill|50/100",
      BattleMinorActionMessage.SetHealth(PokemonId(pos"p1a", Surname("Azumarill")), Health.percent(50))
    )

    test("setStatus") - assertDecodeString(
      decoder,
      "|-setstatus|p1a: Darmanitan|psn",
      BattleMinorActionMessage.SetStatus(PokemonId(pos"p1a", Surname("Darmanitan")), StatusEffect.Poison)
    )

    test("cureStatus") - assertDecodeString(
      decoder,
      "|-curestatus|p1a: Darmanitan|psn",
      BattleMinorActionMessage.CureStatus(PokemonId(pos"p1a", Surname("Darmanitan")), StatusEffect.Poison)
    )

    test("boost") - assertDecodeString(
      decoder,
      "|-boost|p1a: Cloyster|atk|2",
      BattleMinorActionMessage.Boost(
        pokemon = PokemonId(pos"p1a", Surname("Cloyster")),
        stat = StatType.Attack,
        amount = StatBoost(2)
      )
    )

    test("unboost") - assertDecodeString(
      decoder,
      "|-unboost|p1a: Cloyster|def|2",
      BattleMinorActionMessage.Unboost(
        pokemon = PokemonId(pos"p1a", Surname("Cloyster")),
        stat = StatType.Defense,
        amount = StatBoost(2)
      )
    )

    test("setBoost") - assertDecodeString(
      decoder,
      "|-setboost|p1a: Azumarill|atk|6",
      BattleMinorActionMessage.SetBoost(
        pokemon = PokemonId(pos"p1a", Surname("Azumarill")),
        stat = StatType.Attack,
        amount = StatBoost(6)
      )
    )

    test("swapBoost") - assertDecodeString(
      decoder,
      "|-swapboost|p1a: Magearna|p2a: Cloyster|atk,spe,spa",
      BattleMinorActionMessage.SwapBoost(
        pokemon = PokemonId(pos"p1a", Surname("Magearna")),
        target = PokemonId(pos"p2a", Surname("Cloyster")),
        stats = List(StatType.Attack, StatType.Speed, StatType.SpecialAttack)
      )
    )

    test("invertBoost") - assertDecodeString(
      decoder,
      "|-invertboost|p1a: Grapploct",
      BattleMinorActionMessage.InvertBoost(PokemonId(pos"p1a", Surname("Grapploct")))
    )

    test("clearBoost") - assertDecodeString(
      decoder,
      "|-clearboost|p1a: Amoongus",
      BattleMinorActionMessage.ClearBoost(PokemonId(pos"p1a", Surname("Amoongus")))
    )

    test("clearPositiveBoost") - assertDecodeString(
      decoder,
      "|-clearpositiveboost|p2a: Gyarados|p1a: Marshadow|move: Spectral Thief",
      BattleMinorActionMessage.ClearPositiveBoost(
        target = PokemonId(pos"p2a", Surname("Gyarados")),
        pokemon = PokemonId(pos"p1a", Surname("Marshadow")),
        effect = Effect("move: Spectral Thief")
      )
    )

    test("clearNegativeBoost") - assertDecodeString(
      decoder,
      "|-clearnegativeboost|p1a: Porygon-Z|zeffect: Z-Celebrate",
      BattleMinorActionMessage.ClearNegativeBoost(
        pokemon = PokemonId(pos"p1a", Surname("Porygon-Z")),
        effect = Effect("zeffect: Z-Celebrate")
      )
    )

    test("copyBoost") - assertDecodeString(
      decoder,
      "|-copyboost|p1a: Alakazam|p2a: Dragonite",
      BattleMinorActionMessage.CopyBoost(PokemonId(pos"p1a", Surname("Alakazam")), PokemonId(pos"p2a", Surname("Dragonite")))
    )

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
    
    test("volatileStatusStart") - assertDecodeString(
      decoder,
      "|-start|p1a: Garchomp|confusion",
      BattleMinorActionMessage.VolatileStatusStart(PokemonId(pos"p1a", Surname("Garchomp")), VolatileStatus("confusion"))
    )

    test("volatileStatusEnd") - assertDecodeString(
      decoder,
      "|-end|p1a: Garchomp|confusion",
      BattleMinorActionMessage.VolatileStatusEnd(PokemonId(pos"p1a", Surname("Garchomp")), VolatileStatus("confusion"))
    )

    test("superEffective") - assertDecodeString(
      decoder,
      "|-supereffective|p1a: Scizor",
      BattleMinorActionMessage.SuperEffective(PokemonId(pos"p1a", Surname("Scizor")))
    )

    test("resisted") - assertDecodeString(
      decoder,
      "|-resisted|p1a: Scizor",
      BattleMinorActionMessage.Resisted(PokemonId(pos"p1a", Surname("Scizor")))
    )

    test("immune") - assertDecodeString(
      decoder,
      "|-immune|p1a: Gengar",
      BattleMinorActionMessage.Immune(PokemonId(pos"p1a", Surname("Gengar")))
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

    test("waiting") - assertDecodeString(
      decoder,
      "|-waiting|p1a: Typhlosion|p2a: Serperior",
      BattleMinorActionMessage.Waiting(PokemonId(pos"p1a", Surname("Typhlosion")), PokemonId(pos"p2a", Surname("Serperior")))
    )

    test("prepare") - assertDecodeString(
      decoder,
      "|-prepare|p1a: Dragapult|Phantom Force|p2a: Gengar",
      BattleMinorActionMessage.Prepare(
        pokemon = PokemonId(pos"p1a", Surname("Dragapult")),
        move = MoveName("Phantom Force"),
        defender = Some(PokemonId(pos"p2a", Surname("Gengar")))
      )
    )

    test("mustRecharge") - assertDecodeString(
      decoder,
      "|-mustrecharge|p1a: Snorlax",
      BattleMinorActionMessage.MustRecharge(PokemonId(pos"p1a", Surname("Snorlax")))
    )

    test("hitCount") - assertDecodeString(
      decoder,
      "|-hitcount|p1a: Baxcalibur|5",
      BattleMinorActionMessage.HitCount(PokemonId(pos"p1a", Surname("Baxcalibur")), Count(5))
    )

    test("singleMove") - assertDecodeString(
      decoder,
      "|-singlemove|p1a: Celesteela|Destiny Bound",
      BattleMinorActionMessage.SingleMove(PokemonId(pos"p1a", Surname("Celesteela")), MoveName("Destiny Bound"))
    )

    test("singleTurn") - assertDecodeString(
      decoder,
      "|-singleturn|p1a: Celesteela|Protect",
      BattleMinorActionMessage.SingleTurn(PokemonId(pos"p1a", Surname("Celesteela")), MoveName("Protect"))
    )