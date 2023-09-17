package io.github.projectpidove.showdown.protocol.server

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.Timestamp as TimestampValue
import io.github.projectpidove.showdown.battle.{*, given}
import io.github.projectpidove.showdown.protocol.{MessageDecoder, MessageName}
import io.github.projectpidove.showdown.room.ChatContent
import io.github.projectpidove.showdown.team.MoveName
import io.github.projectpidove.showdown.user.{User, Username}

enum BattleProgressMessage derives MessageDecoder:
  @MessageName("") case ClearMessageBar()
  @MessageName("inactive") case TimerMessage(message: ChatContent)
  @MessageName("inactiveoff") case TimerDisabled(message: ChatContent)
  case Upkeep()
  case Turn(number: TurnNumber)
  case Win(user: Username)
  case Tie()
  @MessageName("t:") case Timestamp(timestamp: TimestampValue)
  case Move(pokemon: PokemonId, move: MoveName, target: PokemonId)
  @MessageName("switch", "drag") case Switch(pokemon: PokemonId, details: PokemonDetails, healthStatus: HealthStatus)
  @MessageName("detailschange", "-formechange") case DetailsChange(pokemon: PokemonId, details: PokemonDetails, healthStatus: Option[HealthStatus])
  case Replace(pokemon: PokemonId, details: PokemonDetails, healthStatus: HealthStatus)
  case Swap(pokemon: PokemonId, slot: PokemonSlot)
  @MessageName("cant") case Unable(pokemon: PokemonId, reason: String, move: Option[MoveName])
  case Faint(pokemon: PokemonId)