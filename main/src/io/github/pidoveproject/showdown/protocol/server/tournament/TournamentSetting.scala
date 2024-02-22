package io.github.pidoveproject.showdown.protocol.server.tournament

import io.github.pidoveproject.showdown.protocol.MessageDecoder

/**
 * A tournament setting.
 */
enum TournamentSetting derives MessageDecoder:
  case Allow()
  case Disallow()
