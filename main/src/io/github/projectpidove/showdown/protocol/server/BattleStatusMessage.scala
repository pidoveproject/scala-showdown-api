package io.github.projectpidove.showdown.protocol.server

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.Count
import io.github.projectpidove.showdown.battle.{*, given}
import io.github.projectpidove.showdown.protocol.{MessageDecoder, ProtocolError, messageName, messagePrefix}
import io.github.projectpidove.showdown.room.{ChatContent, given}
import io.github.projectpidove.showdown.team.*
import io.github.projectpidove.showdown.util.either.*

import scala.util.boundary

@messagePrefix("-")
enum BattleStatusMessage derives MessageDecoder:

  /**
   * A pokemon took damage.
   *
   * @param pokemon      the damaged pokemon
   * @param healthStatus the health and current status of the pokemon
   */
  case Damage(pokemon: PokemonId, healthStatus: HealthStatus)

  /**
   * A pokemon was healed.
   *
   * @param pokemon      the healed pokemon
   * @param healthStatus the health and current status of the pokemon
   */
  case Heal(pokemon: PokemonId, healthStatus: HealthStatus)

  /**
   * Set the health of a pokemon.
   *
   * @param pokemon the modified pokemon
   * @param health  the new health of the pokemon
   */
  @messageName("sethp") case SetHealth(pokemon: PokemonId, health: Health)

  /**
   * Set the status of a pokemon.
   *
   * @param pokemon the modified pokemon
   * @param status  the new status of the pokemon
   */
  case SetStatus(pokemon: PokemonId, status: StatusEffect)

  /**
   * Clear the status of a pokemon.
   *
   * @param pokemon the cured pokemon
   * @param status  the removed status
   */
  case CureStatus(pokemon: PokemonId, status: StatusEffect)

  /**
   * Boost a pokemon.
   *
   * @param pokemon the boosted pokemon
   * @param stat    the raised stat
   * @param amount  the number of stage the stat was raised by
   */
  case Boost(pokemon: PokemonId, stat: StatType, amount: StatBoost)

  /**
   * Unboost a pokemon.
   *
   * @param pokemon the unboosted pokemon
   * @param stat    the dropped stat
   * @param amount  the number of stage the stat was dropped by
   */
  case Unboost(pokemon: PokemonId, stat: StatType, amount: StatBoost)

  /**
   * Set the boost of a pokemon.
   *
   * @param pokemon the (un)boosted pokemon
   * @param stat    the modified stat
   * @param amount  the new boost stage of the stat
   */
  case SetBoost(pokemon: PokemonId, stat: StatType, amount: StatBoost)

  /**
   * Swap boost of two pokemon.
   *
   * @param pokemon the pokemon triggering the boost swap
   * @param target  the pokemon affected by the boost swap
   * @param stats   the swapped stats
   */
  case SwapBoost(pokemon: PokemonId, target: PokemonId, stats: List[StatType])

  /**
   * Invert the boost stages of a pokemon.
   *
   * @param pokemon the affected pokemon
   */
  case InvertBoost(pokemon: PokemonId)

  /**
   * Clear the boosts of a pokemon.
   *
   * @param pokemon the affected pokemon
   */
  case ClearBoost(pokemon: PokemonId)

  /**
   * Clear the boosts of all pokemon.
   */
  case ClearAllBoost

  /**
   * Clear the positive boosts of a pokemon.
   *
   * @param target  the affected pokemon
   * @param pokemon the pokemon triggering the boost clear
   * @param effect  the effect causing the boost clear
   */
  case ClearPositiveBoost(target: PokemonId, pokemon: PokemonId, effect: Effect)

  /**
   * Clear the negative boosts of a pokemon.
   *
   * @param pokemon the affected pokemon
   * @param effect  the effect causing the boost clear
   */
  case ClearNegativeBoost(pokemon: PokemonId, effect: Effect)

  /**
   * Copy the boosts of a pokemon.
   *
   * @param pokemon the copying pokemon
   * @param target  the copied pokemon
   */
  case CopyBoost(pokemon: PokemonId, target: PokemonId)

  /**
   * Start a volatile status.
   *
   * @param pokemon the affected pokemon
   * @param status  the started volatile status
   */
  @messageName("start") case VolatileStatusStart(pokemon: PokemonId, status: VolatileStatus)

  /**
   * End a volatile status.
   *
   * @param pokemon the no longer affected pokemon
   * @param status  the ended volatile status
   */
  @messageName("end") case VolatileStatusEnd(pokemon: PokemonId, status: VolatileStatus)

  /**
   * Declare a move effect lasting until another move is used.
   *
   * @param pokemon the affected pokemon
   * @param move    the used move
   */
  case SingleMove(pokemon: PokemonId, move: MoveName)

  /**
   * Declare a move effect lasting until the next turn.
   *
   * @param pokemon the affected pokemon
   * @param move    the used move
   */
  case SingleTurn(pokemon: PokemonId, move: MoveName)

object BattleStatusMessage:

  given MessageDecoder[List[StatType]] = MessageDecoder.string.mapEither: text =>
    boundary[Either[ProtocolError, List[StatType]]]:
      Right(
        text
          .split(",")
          .map(stat => StatType.fromShortName(stat).getOrBreak(ProtocolError.InvalidInput(stat, "Invalid stat type")))
          .toList
      )