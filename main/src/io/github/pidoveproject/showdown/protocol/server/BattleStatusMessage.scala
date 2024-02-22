package io.github.pidoveproject.showdown.protocol.server

import io.github.iltotore.iron.*
import io.github.pidoveproject.showdown.Count
import io.github.pidoveproject.showdown.battle.{*, given}
import io.github.pidoveproject.showdown.protocol.{MessageDecoder, ProtocolError, messageName, messagePrefix}
import io.github.pidoveproject.showdown.room.{ChatContent, given}
import io.github.pidoveproject.showdown.team.*
import io.github.pidoveproject.showdown.util.either.*

import scala.util.boundary

@messagePrefix("-")
enum BattleStatusMessage derives MessageDecoder:

  /**
   * A pokemon took damage.
   *
   * @param pokemon   the damaged pokemon
   * @param condition the health and current status of the pokemon
   */
  case Damage(pokemon: ActiveId, condition: Condition)

  /**
   * A pokemon was healed.
   *
   * @param pokemon   the healed pokemon
   * @param condition the health and current status of the pokemon
   */
  case Heal(pokemon: ActiveId, condition: Condition)

  /**
   * Set the health of a pokemon.
   *
   * @param pokemon the modified pokemon
   * @param health  the new health of the pokemon
   */
  @messageName("sethp") case SetHealth(pokemon: ActiveId, health: Health)

  /**
   * Set the status of a pokemon.
   *
   * @param pokemon the modified pokemon
   * @param status  the new status of the pokemon
   */
  @messageName("status") case SetStatus(pokemon: ActiveId, status: StatusEffect)

  /**
   * Clear the status of a pokemon.
   *
   * @param pokemon the cured pokemon
   * @param status  the removed status
   */
  case CureStatus(pokemon: ActiveId, status: StatusEffect)

  /**
   * Boost a pokemon.
   *
   * @param pokemon the boosted pokemon
   * @param stat    the raised stat
   * @param amount  the number of stage the stat was raised by
   */
  case Boost(pokemon: ActiveId, stat: StatType, amount: StatBoost)

  /**
   * Unboost a pokemon.
   *
   * @param pokemon the unboosted pokemon
   * @param stat    the dropped stat
   * @param amount  the number of stage the stat was dropped by
   */
  case Unboost(pokemon: ActiveId, stat: StatType, amount: StatBoost)

  /**
   * Set the boost of a pokemon.
   *
   * @param pokemon the (un)boosted pokemon
   * @param stat    the modified stat
   * @param amount  the new boost stage of the stat
   */
  case SetBoost(pokemon: ActiveId, stat: StatType, amount: StatBoost)

  /**
   * Swap boost of two pokemon.
   *
   * @param pokemon the pokemon triggering the boost swap
   * @param target  the pokemon affected by the boost swap
   * @param stats   the swapped stats
   */
  case SwapBoost(pokemon: ActiveId, target: ActiveId, stats: List[StatType])

  /**
   * Invert the boost stages of a pokemon.
   *
   * @param pokemon the affected pokemon
   */
  case InvertBoost(pokemon: ActiveId)

  /**
   * Clear the boosts of a pokemon.
   *
   * @param pokemon the affected pokemon
   */
  case ClearBoost(pokemon: ActiveId)

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
  case ClearPositiveBoost(target: ActiveId, pokemon: ActiveId, effect: Effect)

  /**
   * Clear the negative boosts of a pokemon.
   *
   * @param pokemon the affected pokemon
   * @param effect  the effect causing the boost clear
   */
  case ClearNegativeBoost(pokemon: ActiveId, effect: Effect)

  /**
   * Copy the boosts of a pokemon.
   *
   * @param pokemon the copying pokemon
   * @param target  the copied pokemon
   */
  case CopyBoost(pokemon: ActiveId, target: ActiveId)

  /**
   * Start a volatile status.
   *
   * @param pokemon the affected pokemon
   * @param status  the started volatile status
   */
  @messageName("start") case VolatileStatusStart(pokemon: ActiveId, status: VolatileStatus)

  /**
   * End a volatile status.
   *
   * @param pokemon the no longer affected pokemon
   * @param status  the ended volatile status
   */
  @messageName("end") case VolatileStatusEnd(pokemon: ActiveId, status: VolatileStatus)

  /**
   * Declare a move effect lasting until another move is used.
   *
   * @param pokemon the affected pokemon
   * @param move    the used move
   */
  case SingleMove(pokemon: ActiveId, move: MoveName)

  /**
   * Declare a move effect lasting until the next turn.
   *
   * @param pokemon the affected pokemon
   * @param move    the used move
   */
  case SingleTurn(pokemon: ActiveId, move: MoveName)

object BattleStatusMessage:

  given MessageDecoder[List[StatType]] = MessageDecoder.string.mapEither: text =>
    boundary[Either[ProtocolError, List[StatType]]]:
      Right(
        text
          .split(",")
          .map(stat => StatType.fromShortName(stat).getOrBreak(ProtocolError.InvalidInput(stat, "Invalid stat type")))
          .toList
      )