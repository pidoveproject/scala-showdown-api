package io.github.projectpidove.showdown.protocol.server.tournament

import io.github.projectpidove.showdown.protocol.MessageDecoder

enum TournamentSetting derives MessageDecoder:
  case Allow()
  case Disallow()
