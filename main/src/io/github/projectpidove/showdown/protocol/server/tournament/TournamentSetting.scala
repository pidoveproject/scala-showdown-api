package io.github.projectpidove.showdown.protocol.server.tournament

import io.github.projectpidove.showdown.protocol.MessageDecoder

/**
 * A tournament setting.
 */
enum TournamentSetting derives MessageDecoder:
  case Allow()
  case Disallow()
