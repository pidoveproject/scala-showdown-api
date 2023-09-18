package io.github.projectpidove.showdown.testing.protocol.server

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.projectpidove.showdown.battle.*
import io.github.projectpidove.showdown.battle.PokemonPosition.pos
import io.github.projectpidove.showdown.protocol.MessageDecoder
import io.github.projectpidove.showdown.protocol.server.BattleMinorActionMessage
import io.github.projectpidove.showdown.testing.protocol.*
import io.github.projectpidove.showdown.team.*
import utest.*

object BattleMinorActionSuite extends TestSuite:
  
  val tests = Tests:
    
    val decoder = summon[MessageDecoder[BattleMinorActionMessage]]

    test("fail") - assertDecodeString(
      decoder,
      "|-fail|p1a: Froslass|Destiny Bound",
      BattleMinorActionMessage.Fail(PokemonId(pos"p1a", Surname("Froslass")), MoveName("Destiny Bound"))
    )

    