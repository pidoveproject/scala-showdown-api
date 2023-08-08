package io.github.projectpidove.showdown.protocol.server.tournament

import io.github.projectpidove.showdown.protocol.MessageDecoder
import io.github.projectpidove.showdown.protocol.MessageDecoder.given
enum TournamentRecord:
  case Fail()
  case Success()

object TournamentRecord:

  given MessageDecoder[TournamentRecord] = MessageDecoder.derived[TournamentRecord]
