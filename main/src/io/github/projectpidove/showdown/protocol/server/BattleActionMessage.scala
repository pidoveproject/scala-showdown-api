package io.github.projectpidove.showdown.protocol.server

import io.github.projectpidove.showdown.battle.PokemonId
import io.github.projectpidove.showdown.protocol.MessageDecoder
import io.github.projectpidove.showdown.team.MoveName

enum BattleActionMessage derives MessageDecoder:
  case Move(attacker: PokemonId, move: MoveName, target: PokemonId)