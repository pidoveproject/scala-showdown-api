package io.github.projectpidove.showdown.protocol.server.tournament

import io.github.projectpidove.showdown.protocol.MessageDecoder

/**
 * The result of a tournament record.
 */
enum TournamentRecord derives MessageDecoder:
  case Fail()
  case Success()
