package io.github.projectpidove.showdown.protocol.server.tournament

import io.github.projectpidove.showdown.protocol.MessageDecoder
import io.github.projectpidove.showdown.protocol.MessageDecoder.given

enum TournamentSetting:
  case Allow()
  case Disallow()

object TournamentSetting:

  given MessageDecoder[TournamentSetting] = MessageDecoder.derived[TournamentSetting]
