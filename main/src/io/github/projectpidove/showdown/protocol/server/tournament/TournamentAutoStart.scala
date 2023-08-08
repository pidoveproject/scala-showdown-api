package io.github.projectpidove.showdown.protocol.server.tournament

import io.github.projectpidove.showdown.Timestamp
import io.github.projectpidove.showdown.protocol.MessageDecoder
import io.github.projectpidove.showdown.protocol.MessageDecoder.given

enum TournamentAutoStart:
  case On(startTimer: Timestamp)
  case Off()

object TournamentAutoStart:

  given MessageDecoder[TournamentAutoStart] = MessageDecoder.derived[TournamentAutoStart]
