package io.github.projectpidove.showdown.protocol.server.tournament

import io.github.projectpidove.showdown.protocol.MessageDecoder

enum TournamentRecord derives MessageDecoder:
  case Fail()
  case Success()

