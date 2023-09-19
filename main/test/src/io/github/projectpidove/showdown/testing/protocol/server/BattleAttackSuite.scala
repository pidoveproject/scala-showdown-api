package io.github.projectpidove.showdown.testing.protocol.server

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.projectpidove.showdown.Count
import io.github.projectpidove.showdown.battle.*
import io.github.projectpidove.showdown.battle.PokemonPosition.pos
import io.github.projectpidove.showdown.protocol.MessageDecoder
import io.github.projectpidove.showdown.protocol.server.BattleAttackMessage
import io.github.projectpidove.showdown.room.ChatContent
import io.github.projectpidove.showdown.testing.protocol.*
import io.github.projectpidove.showdown.team.*
import io.github.projectpidove.showdown.user.Username
import utest.*

object BattleAttackSuite extends TestSuite:

  val tests = Tests:

    val decoder = summon[MessageDecoder[BattleAttackMessage]]

    test("fail") - assertDecodeString(
      decoder,
      "|-fail|p1a: Froslass|Destiny Bound",
      BattleAttackMessage.Fail(PokemonId(pos"p1a", Surname("Froslass")), MoveName("Destiny Bound"))
    )

    test("block") - assertDecodeString(
      decoder,
      "|-block|p1a: Celesteela|Protect|p2a: Nidoking",
      BattleAttackMessage.Block(
        pokemon = PokemonId(pos"p1a", Surname("Celesteela")),
        effect = Effect("Protect"),
        attacker = PokemonId(pos"p2a", Surname("Nidoking"))
      )
    )

    test("noTarget") - assertDecodeString(
      decoder,
      "|-notarget|p1a: Girafarig",
      BattleAttackMessage.NoTarget(PokemonId(pos"p1a", Surname("Girafarig")))
    )

    test("miss") - assertDecodeString(
      decoder,
      "|-miss|p1a: Gholdengo|p2a: Garganacl",
      BattleAttackMessage.Miss(PokemonId(pos"p1a", Surname("Gholdengo")), PokemonId(pos"p2a", Surname("Garganacl")))
    )

    test("superEffective") - assertDecodeString(
      decoder,
      "|-supereffective|p1a: Scizor",
      BattleAttackMessage.SuperEffective(PokemonId(pos"p1a", Surname("Scizor")))
    )

    test("resisted") - assertDecodeString(
      decoder,
      "|-resisted|p1a: Scizor",
      BattleAttackMessage.Resisted(PokemonId(pos"p1a", Surname("Scizor")))
    )

    test("immune") - assertDecodeString(
      decoder,
      "|-immune|p1a: Gengar",
      BattleAttackMessage.Immune(PokemonId(pos"p1a", Surname("Gengar")))
    )

    test("waiting") - assertDecodeString(
      decoder,
      "|-waiting|p1a: Typhlosion|p2a: Serperior",
      BattleAttackMessage.Waiting(PokemonId(pos"p1a", Surname("Typhlosion")), PokemonId(pos"p2a", Surname("Serperior")))
    )

    test("prepare") - assertDecodeString(
      decoder,
      "|-prepare|p1a: Dragapult|Phantom Force|p2a: Gengar",
      BattleAttackMessage.Prepare(
        pokemon = PokemonId(pos"p1a", Surname("Dragapult")),
        move = MoveName("Phantom Force"),
        defender = Some(PokemonId(pos"p2a", Surname("Gengar")))
      )
    )

    test("mustRecharge") - assertDecodeString(
      decoder,
      "|-mustrecharge|p1a: Snorlax",
      BattleAttackMessage.MustRecharge(PokemonId(pos"p1a", Surname("Snorlax")))
    )

    test("hitCount") - assertDecodeString(
      decoder,
      "|-hitcount|p1a: Baxcalibur|5",
      BattleAttackMessage.HitCount(PokemonId(pos"p1a", Surname("Baxcalibur")), Count(5))
    )