package io.github.pidoveproject.showdown.testing.protocol.server

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.pidoveproject.showdown.battle.*
import io.github.pidoveproject.showdown.battle.ActivePosition.pos
import io.github.pidoveproject.showdown.protocol.MessageDecoder
import io.github.pidoveproject.showdown.protocol.server.BattleStatusMessage
import io.github.pidoveproject.showdown.testing.protocol.*
import io.github.pidoveproject.showdown.team.*
import utest.*

object BattleStatusSuite extends TestSuite:

  val tests = Tests:

    val decoder = summon[MessageDecoder[BattleStatusMessage]]

    test("damage") - assertDecodeString(
      decoder,
      "|-damage|p1a: Gholdengo|12/100",
      BattleStatusMessage.Damage(ActiveId(pos"p1a", Surname("Gholdengo")), Condition(Health.percent(12)))
    )

    test("heal") - assertDecodeString(
      decoder,
      "|-heal|p1a: Gholdengo|6/100",
      BattleStatusMessage.Heal(ActiveId(pos"p1a", Surname("Gholdengo")), Condition(Health.percent(6)))
    )

    test("setHealth") - assertDecodeString(
      decoder,
      "|-sethp|p1a: Azumarill|50/100",
      BattleStatusMessage.SetHealth(ActiveId(pos"p1a", Surname("Azumarill")), Health.percent(50))
    )

    test("setStatus") - assertDecodeString(
      decoder,
      "|-status|p1a: Darmanitan|psn",
      BattleStatusMessage.SetStatus(ActiveId(pos"p1a", Surname("Darmanitan")), StatusEffect.Poison)
    )

    test("cureStatus") - assertDecodeString(
      decoder,
      "|-curestatus|p1a: Darmanitan|psn",
      BattleStatusMessage.CureStatus(ActiveId(pos"p1a", Surname("Darmanitan")), StatusEffect.Poison)
    )

    test("boost") - assertDecodeString(
      decoder,
      "|-boost|p1a: Cloyster|atk|2",
      BattleStatusMessage.Boost(
        pokemon = ActiveId(pos"p1a", Surname("Cloyster")),
        stat = StatType.Attack,
        amount = StatBoost(2)
      )
    )

    test("unboost") - assertDecodeString(
      decoder,
      "|-unboost|p1a: Cloyster|def|2",
      BattleStatusMessage.Unboost(
        pokemon = ActiveId(pos"p1a", Surname("Cloyster")),
        stat = StatType.Defense,
        amount = StatBoost(2)
      )
    )

    test("setBoost") - assertDecodeString(
      decoder,
      "|-setboost|p1a: Azumarill|atk|6",
      BattleStatusMessage.SetBoost(
        pokemon = ActiveId(pos"p1a", Surname("Azumarill")),
        stat = StatType.Attack,
        amount = StatBoost(6)
      )
    )

    test("swapBoost") - assertDecodeString(
      decoder,
      "|-swapboost|p1a: Magearna|p2a: Cloyster|atk,spe,spa",
      BattleStatusMessage.SwapBoost(
        pokemon = ActiveId(pos"p1a", Surname("Magearna")),
        target = ActiveId(pos"p2a", Surname("Cloyster")),
        stats = List(StatType.Attack, StatType.Speed, StatType.SpecialAttack)
      )
    )

    test("invertBoost") - assertDecodeString(
      decoder,
      "|-invertboost|p1a: Grapploct",
      BattleStatusMessage.InvertBoost(ActiveId(pos"p1a", Surname("Grapploct")))
    )

    test("clearBoost") - assertDecodeString(
      decoder,
      "|-clearboost|p1a: Amoongus",
      BattleStatusMessage.ClearBoost(ActiveId(pos"p1a", Surname("Amoongus")))
    )

    test("clearPositiveBoost") - assertDecodeString(
      decoder,
      "|-clearpositiveboost|p2a: Gyarados|p1a: Marshadow|[from] move: Spectral Thief",
      BattleStatusMessage.ClearPositiveBoost(
        target = ActiveId(pos"p2a", Surname("Gyarados")),
        pokemon = ActiveId(pos"p1a", Surname("Marshadow")),
        effect = Effect.Move(MoveName("Spectral Thief"))
      )
    )

    test("clearNegativeBoost") - assertDecodeString(
      decoder,
      "|-clearnegativeboost|p1a: Porygon-Z|[from] zeffect: Z-Celebrate",
      BattleStatusMessage.ClearNegativeBoost(
        pokemon = ActiveId(pos"p1a", Surname("Porygon-Z")),
        effect = Effect.ZEffect(MoveName("Z-Celebrate"))
      )
    )

    test("copyBoost") - assertDecodeString(
      decoder,
      "|-copyboost|p1a: Alakazam|p2a: Dragonite",
      BattleStatusMessage.CopyBoost(ActiveId(pos"p1a", Surname("Alakazam")), ActiveId(pos"p2a", Surname("Dragonite")))
    )

    test("volatileStatusStart") - assertDecodeString(
      decoder,
      "|-start|p1a: Garchomp|confusion",
      BattleStatusMessage.VolatileStatusStart(ActiveId(pos"p1a", Surname("Garchomp")), VolatileStatus("confusion"))
    )

    test("volatileStatusEnd") - assertDecodeString(
      decoder,
      "|-end|p1a: Garchomp|confusion",
      BattleStatusMessage.VolatileStatusEnd(ActiveId(pos"p1a", Surname("Garchomp")), VolatileStatus("confusion"))
    )

    test("singleMove") - assertDecodeString(
      decoder,
      "|-singlemove|p1a: Celesteela|Destiny Bound",
      BattleStatusMessage.SingleMove(ActiveId(pos"p1a", Surname("Celesteela")), MoveName("Destiny Bound"))
    )

    test("singleTurn") - assertDecodeString(
      decoder,
      "|-singleturn|p1a: Celesteela|Protect",
      BattleStatusMessage.SingleTurn(ActiveId(pos"p1a", Surname("Celesteela")), MoveName("Protect"))
    )
