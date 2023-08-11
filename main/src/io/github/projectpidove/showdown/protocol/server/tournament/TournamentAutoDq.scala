package io.github.projectpidove.showdown.protocol.server.tournament

import io.github.projectpidove.showdown.Timestamp
import io.github.projectpidove.showdown.protocol.MessageDecoder
import io.github.projectpidove.showdown.protocol.MessageDecoder.given

enum TournamentAutoDq:
  case On(disqualifyTimer: Timestamp)
  case Off()
  case Target(time: Timestamp)

object TournamentAutoDq:

  given MessageDecoder[TournamentAutoDq] = MessageDecoder.derived[TournamentAutoDq]
