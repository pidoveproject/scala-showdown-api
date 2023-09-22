package io.github.projectpidove.showdown.battle

import io.github.projectpidove.showdown.user.Username

enum BattleResult:
  case Win(winner: Username)
  case Tie