package io.github.projectpidove.showdown.protocol.server.tournament

import io.github.projectpidove.showdown.protocol.MessageDecoder
import io.github.projectpidove.showdown.protocol.MessageDecoder.given
import zio.json.JsonDecoder

enum BattleResult derives JsonDecoder:
  case Win()
  case Loss()
  case Draw()

object BattleResult:

  given MessageDecoder[BattleResult] = MessageDecoder.derived[BattleResult]
