package io.github.projectpidove.showdown.protocol.server.tournament

import io.github.projectpidove.showdown.Count
import io.github.projectpidove.showdown.protocol.MessageDecoder
import io.github.projectpidove.showdown.protocol.MessageDecoder.given

enum TournamentGenerator:
  case Elimination(maxLoss: Int)
  case RoundRobin(maxEncounter: Count)

object TournamentGenerator:

  given MessageDecoder[TournamentGenerator] = MessageDecoder.derived[TournamentGenerator]
