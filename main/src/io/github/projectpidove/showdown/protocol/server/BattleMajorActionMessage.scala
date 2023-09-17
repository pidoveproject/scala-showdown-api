package io.github.projectpidove.showdown.protocol.server

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.battle.{*, given}
import io.github.projectpidove.showdown.protocol.{MessageDecoder, messageName}
import io.github.projectpidove.showdown.team.MoveName

enum BattleMajorActionMessage derives MessageDecoder:
  case Move(pokemon: PokemonId, move: MoveName, target: PokemonId)
  @messageName("switch", "drag") case Switch(pokemon: PokemonId, details: PokemonDetails, healthStatus: HealthStatus)
  @messageName("detailschange", "-formechange") case DetailsChange(pokemon: PokemonId, details: PokemonDetails, healthStatus: Option[HealthStatus])
  case Replace(pokemon: PokemonId, details: PokemonDetails, healthStatus: HealthStatus)
  case Swap(pokemon: PokemonId, slot: PokemonSlot)
  @messageName("cant") case Unable(pokemon: PokemonId, reason: String, move: Option[MoveName])
  case Faint(pokemon: PokemonId)