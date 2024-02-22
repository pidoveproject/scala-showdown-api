package io.github.pidoveproject.showdown.protocol.server.tournament

import io.github.pidoveproject.showdown.protocol.MessageDecoder

/**
 * The result of a tournament record.
 */
enum TournamentRecord derives MessageDecoder:
  case Fail()
  case Success()
