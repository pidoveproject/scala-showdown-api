package io.github.projectpidove.showdown.protocol.server.tournament

import io.github.projectpidove.showdown.Timestamp
import io.github.projectpidove.showdown.protocol.MessageDecoder

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
