package io.github.projectpidove.showdown.battle

import io.github.projectpidove.showdown.user.Username

/**
 * The result of an ended battle.
 */
enum BattleResult:

  /**
   * A user won the game.
   * 
   * @param winner the user who won
   */
  case Win(winner: Username)

  /**
   * The game ended in a tie.
   */
  case Tie