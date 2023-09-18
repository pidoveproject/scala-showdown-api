package io.github.projectpidove.showdown.protocol.server

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.Count
import io.github.projectpidove.showdown.battle.{Weather as WeatherEffect, *, given}
import io.github.projectpidove.showdown.protocol.{MessageDecoder, ProtocolError, messageName, messagePrefix}
import io.github.projectpidove.showdown.room.{ChatContent, given}
import io.github.projectpidove.showdown.team.*
import io.github.projectpidove.showdown.util.either.*

import scala.util.boundary

@messagePrefix("-")
enum BattleMinorActionMessage derives MessageDecoder:
  case Fail(pokemon: PokemonId, move: MoveName)
  case Block(pokemon: PokemonId, effect: Effect, attacker: PokemonId)
  case NoTarget(pokemon: PokemonId)
  case Miss(pokemon: PokemonId, target: PokemonId)
  case Damage(pokemon: PokemonId, healthStatus: HealthStatus)
  case Heal(pokemon: PokemonId, healthStatus: HealthStatus)
  @messageName("sethp") case SetHealth(pokemon: PokemonId, health: Health)
  case SetStatus(pokemon: PokemonId, status: StatusEffect)
  case CureStatus(pokemon: PokemonId, status: StatusEffect)
  case Boost(pokemon: PokemonId, stat: StatType, amount: StatBoost)
  case Unboost(pokemon: PokemonId, stat: StatType, amount: StatBoost)
  case SetBoost(pokemon: PokemonId, stat: StatType, amount: StatBoost)
  case SwapBoost(pokemon: PokemonId, target: PokemonId, stats: List[StatType])
  case InvertBoost(pokemon: PokemonId)
  case ClearBoost(pokemon: PokemonId)
  case ClearAllBoost
  case ClearPositiveBoost(target: PokemonId, pokemon: PokemonId, effect: Effect)
  case ClearNegativeBoost(target: PokemonId, pokemon: PokemonId, effect: Effect)
  case CopyBoost(pokemon: PokemonId, target: PokemonId)
  case Weather(weather: WeatherEffect)
  case FieldStart(field: FieldEffect)
  case FieldEnd(field: FieldEffect)
  case SideStart(side: PlayerPosition, field: SideFieldEffect)
  case SideEnd(side: PlayerPosition, field: SideFieldEffect)
  case SwapSideConditions
  @messageName("start") case VolatileStatusStart(pokemon: PokemonId, status: VolatileStatus)
  @messageName("") case VolatileStatusEnd(pokemon: PokemonId, status: VolatileStatus)
  @messageName("crit") case CriticalHit
  case SuperEffective(pokemon: PokemonId)
  case Resisted(pokemon: PokemonId)
  case Immune(pokemon: PokemonId)
  case Item(pokemon: PokemonId, item: ItemName, effect: Option[Effect])
  case EndItem(pokemon: PokemonId, item: ItemName, effect: Option[Effect])
  case Ability(pokemon: PokemonId, ability: AbilityName, effect: Option[Effect])
  case EndAbility(pokemon: PokemonId)
  case Transform(pokemon: PokemonId, species: SpeciesName)
  case Mega(pokemon: PokemonId, megaStone: ItemName)
  @messageName("burst") case UltraBurst(pokemon: PokemonId, species: SpeciesName, item: ItemName)
  case ZPower(pokemon: PokemonId)
  case ZBroken(pokemon: PokemonId)
  case Activate(effect: Effect)
  case Hint(message: ChatContent)
  case Center
  case Message(message: ChatContent)
  case Combine
  case Waiting(pokemon: PokemonId, target: PokemonId)
  case Prepare(pokemon: PokemonId, move: MoveName, defender: Option[PokemonId])
  case MustRecharge(pokemon: PokemonId)
  @messageName("nothing") case NothingHappened
  case HitCount(pokemon: PokemonId, count: Count)
  case SingleTurn(pokemon: PokemonId, move: MoveName)

object BattleMinorActionMessage:

  summon[MessageDecoder[PlayerPosition]]

  given MessageDecoder[List[StatType]] = MessageDecoder.string.mapEither: text =>
    boundary[Either[ProtocolError, List[StatType]]]:
      Right(
        text
          .split(",")
          .map(stat => StatType.fromShortName(stat).getOrBreak(ProtocolError.InvalidInput(stat, "Invalid stat type")))
          .toList
      )