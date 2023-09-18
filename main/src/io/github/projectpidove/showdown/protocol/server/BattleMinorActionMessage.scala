package io.github.projectpidove.showdown.protocol.server

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.Count
import io.github.projectpidove.showdown.battle.{Weather as WeatherEffect, *, given}
import io.github.projectpidove.showdown.protocol.{MessageDecoder, ProtocolError, messageName, messagePrefix}
import io.github.projectpidove.showdown.room.{ChatContent, given}
import io.github.projectpidove.showdown.team.*
import io.github.projectpidove.showdown.util.either.*

import scala.util.boundary


/**
 * A major action on an active battle.
 */
@messagePrefix("-")
enum BattleMinorActionMessage derives MessageDecoder:

  /**
   * A move failed.
   *
   * @param pokemon the move user
   * @param move the failed move
   */
  case Fail(pokemon: PokemonId, move: MoveName)

  /**
   * An effect blocked a move.
   *
   * @param pokemon the pokemon who blocked the move
   * @param effect the blocking effect
   * @param attacker the blocked pokemon
   */
  case Block(pokemon: PokemonId, effect: Effect, attacker: PokemonId)

  /**
   * A pokemon does not have any target.
   *
   * @param pokemon the pokemon who tried to attack
   */
  case NoTarget(pokemon: PokemonId)

  /**
   * A pokemon avoided an attack
   *
   * @param pokemon the move user
   * @param target the pokemon who avoided the attack
   */
  case Miss(pokemon: PokemonId, target: PokemonId)

  /**
   * A pokemon took damage.
   *
   * @param pokemon the damaged pokemon
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
   * @param status the new status of the pokemon
   */
  case SetStatus(pokemon: PokemonId, status: StatusEffect)

  /**
   * Clear the status of a pokemon.
   *
   * @param pokemon the cured pokemon
   * @param status the removed status
   */
  case CureStatus(pokemon: PokemonId, status: StatusEffect)

  /**
   * Boost a pokemon.
   *
   * @param pokemon the boosted pokemon
   * @param stat the raised stat
   * @param amount the number of stage the stat was raised by
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
   * @param target the pokemon affected by the boost swap
   * @param stats the swapped stats
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
   * @param target the affected pokemon
   * @param pokemon the pokemon triggering the boost clear
   * @param effect the effect causing the boost clear
   */
  case ClearPositiveBoost(target: PokemonId, pokemon: PokemonId, effect: Effect)

  /**
   * Clear the negative boosts of a pokemon.
   *
   * @param pokemon  the affected pokemon
   * @param effect   the effect causing the boost clear
   */
  case ClearNegativeBoost(pokemon: PokemonId, effect: Effect)

  /**
   * Copy the boosts of a pokemon.
   *
   * @param pokemon the copying pokemon
   * @param target the copied pokemon
   */
  case CopyBoost(pokemon: PokemonId, target: PokemonId)

  /**
   * Set or clear the active weather.
   *
   * @param weather the active weather or clear if absent
   */
  case Weather(weather: Option[WeatherEffect])

  /**
   * Start a field effect.
   *
   * @param field the started field effect
   */
  case FieldStart(field: FieldEffect)

  /**
   * End a field effect.
   *
   * @param field the ended field effect
   */
  case FieldEnd(field: FieldEffect)

  /**
   * Start a side-dependent field effect.
   *
   * @param side the side of the effect
   * @param field the field effect
   */
  case SideStart(side: PlayerPosition, field: SideFieldEffect)

  /**
   * End a side-dependent field effect.
   *
   * @param side the side of the effect
   * @param field the field effect
   */
  case SideEnd(side: PlayerPosition, field: SideFieldEffect)

  /**
   * Flip side of field effects (aka Court Change).
   */
  case SwapSideConditions

  /**
   * Start a volatile status.
   *
   * @param pokemon the affected pokemon
   * @param status the started volatile status
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
   * A pokemon made a critical hit.
   */
  @messageName("crit") case CriticalHit

  /**
   * A move was super effective.
   *
   * @param pokemon the defender
   */
  case SuperEffective(pokemon: PokemonId)

  /**
   * A move was not very effective.
   *
   * @param pokemon the defender
   */
  case Resisted(pokemon: PokemonId)

  /**
   * A pokemon is immune to the used move.
   *
   * @param pokemon the defender
   */
  case Immune(pokemon: PokemonId)

  /**
   * An item was revealed.
   *
   * @param pokemon the item holder
   * @param item the revealed item
   * @param effect the optional cause of the reveal
   */
  case Item(pokemon: PokemonId, item: ItemName, effect: Option[Effect])

  /**
   * An item was destroyed.
   *
   * @param pokemon the item holder
   * @param item    the revealed item
   * @param effect  the optional cause of the destruction
   */
  case EndItem(pokemon: PokemonId, item: ItemName, effect: Option[Effect])

  /**
   * An ability was revealed.
   *
   * @param pokemon the ability holder
   * @param ability the revealed ability
   * @param effect the optional cause of the reveal
   */
  case Ability(pokemon: PokemonId, ability: AbilityName, effect: Option[Effect])

  /**
   * An ability was destroyed.
   *
   * @param pokemon the ability holder
   * @param ability the revealed ability
   * @param effect  the optional cause of the destruction
   */
  case EndAbility(pokemon: PokemonId)

  /**
   * A pokemon transformed into another species.
   *
   * @param pokemon the transformed pokemon
   * @param species the new species of the pokemon
   */
  case Transform(pokemon: PokemonId, species: SpeciesName)

  /**
   * A pokemon mega-evolved.
   *
   * @param pokemon the mega-evolving pokemon
   * @param megaStone the mega stone held by the pokemon
   */
  case Mega(pokemon: PokemonId, megaStone: ItemName)

  /**
   * A pokemon ultra-bursted.
   *
   * @param pokemon the ultra-bursting pokemon
   * @param species the new species of the pokemon (e.g Ultra-Necrozma)
   * @param item the item causing the ultra burst
   */
  @messageName("burst") case UltraBurst(pokemon: PokemonId, species: SpeciesName, item: ItemName)

  /**
   * A pokemon used its Z power.
   *
   * @param pokemon the pokemon using its Z power
   */
  case ZPower(pokemon: PokemonId)

  /**
   * A Z move was countered by a protection move.
   *
   * @param pokemon the pokemon who used its Z power
   */
  case ZBroken(pokemon: PokemonId)

  /**
   * An effect was triggered.
   *
   * @param effect the triggered effect
   */
  case Activate(effect: Effect)

  /**
   * A hint on why an event happened (usually not displayed in Pokemon games).
   *
   * @param message the hint content
   */
  case Hint(message: ChatContent)

  /**
   * Center pokemon automatically in Triple Battle when only one pokemon is remaining on each side.
   */
  case Center

  /**
   * A miscellaneous message sent by the simulator.
   */
  case Message(message: ChatContent)

  /**
   * The move has been combined with another (e.g Fire Pledge).
   */
  case Combine

  /**
   * A move user is waiting for the target.
   *
   * @param pokemon the move user
   * @param target the waited-for pokemon
   */
  case Waiting(pokemon: PokemonId, target: PokemonId)

  /**
   * A pokemon is charging a move on a potentially unknown target.
   *
   * @param pokemon the attacking pokemon
   * @param move the charging move
   * @param defender the move target if known
   */
  case Prepare(pokemon: PokemonId, move: MoveName, defender: Option[PokemonId])

  /**
   * A pokemon must recharge (e.g after using Hyper Beam).
   *
   * @param pokemon the pokemon needing to recharge
   */
  case MustRecharge(pokemon: PokemonId)

  /**
   * Deprecated message to indicate that nothing happened.
   */
  @messageName("nothing") case NothingHappened

  /**
   * A pokemon hit n times.
   *
   * @param pokemon the attacker
   * @param count the number of time the pokemon hit
   */
  case HitCount(pokemon: PokemonId, count: Count)

  /**
   * Declare a move effect lasting until another move is used.
   *
   * @param pokemon the affected pokemon
   * @param move the used move
   */
  case SingleMove(pokemon: PokemonId, move: MoveName)

  /**
   * Declare a move effect lasting until the next turn.
   *
   * @param pokemon the affected pokemon
   * @param move    the used move
   */
  case SingleTurn(pokemon: PokemonId, move: MoveName)

object BattleMinorActionMessage:

  given MessageDecoder[List[StatType]] = MessageDecoder.string.mapEither: text =>
    boundary[Either[ProtocolError, List[StatType]]]:
      Right(
        text
          .split(",")
          .map(stat => StatType.fromShortName(stat).getOrBreak(ProtocolError.InvalidInput(stat, "Invalid stat type")))
          .toList
      )
      
  given (using weatherDecoder: MessageDecoder[Weather]): MessageDecoder[Option[Weather]] =
    MessageDecoder.word("none").map(_ => None) <> weatherDecoder.map(Some.apply)