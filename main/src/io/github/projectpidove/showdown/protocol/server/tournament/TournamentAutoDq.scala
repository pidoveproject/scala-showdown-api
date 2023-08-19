package io.github.projectpidove.showdown.protocol.server.tournament

import io.github.projectpidove.showdown.Timestamp
import io.github.projectpidove.showdown.protocol.MessageDecoder

/**
 * The auto-disqualification policy for tournaments.
 */
enum TournamentAutoDq derives MessageDecoder:

  /**
   * Disqualify inactive players each `disqualifyTimer` time.
   */
  case On(disqualifyTimer: Timestamp)

  /**
   * Disabled.
   */
  case Off()

  /**
   * Disqualify players AFK for `disqualifyTimer`.
   */
  case Target(time: Timestamp)
