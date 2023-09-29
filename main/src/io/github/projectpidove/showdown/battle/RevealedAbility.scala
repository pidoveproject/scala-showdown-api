package io.github.projectpidove.showdown.battle

import io.github.projectpidove.showdown.team.AbilityName

enum RevealedAbility:
  case Base(ability: AbilityName)
  case Modified(ability: AbilityName, cause: Effect)
  case Disabled