package io.github.pidoveproject.showdown.protocol.server.tournament

import io.github.pidoveproject.showdown.Timestamp
import io.github.pidoveproject.showdown.protocol.MessageDecoder

/**
 * The auto-start policy for tournaments
 */
enum TournamentAutoStart derives MessageDecoder:

  /**
   * Start the tournament after `startTimer` time.
   */
  case On(startTimer: Timestamp)

  /**
   * Disabled.
   */
  case Off()
