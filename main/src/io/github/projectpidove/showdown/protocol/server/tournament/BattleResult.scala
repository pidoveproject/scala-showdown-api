package io.github.projectpidove.showdown.protocol.server.tournament

import io.github.projectpidove.showdown.protocol.MessageDecoder
import io.github.projectpidove.showdown.protocol.MessageDecoder.given

enum BattleResult:
  case Win()
  case Loss()
  case Draw()

object BattleResult:

  given MessageDecoder[BattleResult] = MessageDecoder.derived[BattleResult]
