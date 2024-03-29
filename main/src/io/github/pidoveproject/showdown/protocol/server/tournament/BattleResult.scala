package io.github.pidoveproject.showdown.protocol.server.tournament

import io.github.pidoveproject.showdown.protocol.MessageDecoder
import zio.json.JsonDecoder

/**
 * The result of a pokemon battle
 */
enum BattleResult derives JsonDecoder, MessageDecoder:
  case Win()
  case Loss()
  case Draw()
