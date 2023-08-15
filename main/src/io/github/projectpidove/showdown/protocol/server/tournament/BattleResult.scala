package io.github.projectpidove.showdown.protocol.server.tournament

import io.github.projectpidove.showdown.protocol.MessageDecoder
import zio.json.JsonDecoder

enum BattleResult derives JsonDecoder, MessageDecoder:
  case Win()
  case Loss()
  case Draw()
