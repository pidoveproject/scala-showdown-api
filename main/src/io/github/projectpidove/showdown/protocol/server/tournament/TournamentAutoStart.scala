package io.github.projectpidove.showdown.protocol.server.tournament

import io.github.projectpidove.showdown.Timestamp
import io.github.projectpidove.showdown.protocol.MessageDecoder

enum TournamentAutoStart derives MessageDecoder:
  case On(startTimer: Timestamp)
  case Off()