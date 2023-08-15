package io.github.projectpidove.showdown.protocol.server.tournament

import io.github.projectpidove.showdown.Timestamp
import io.github.projectpidove.showdown.protocol.MessageDecoder

enum TournamentAutoDq derives MessageDecoder:
  case On(disqualifyTimer: Timestamp)
  case Off()
  case Target(time: Timestamp)
