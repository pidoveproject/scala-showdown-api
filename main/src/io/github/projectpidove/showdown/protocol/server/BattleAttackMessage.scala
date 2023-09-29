package io.github.projectpidove.showdown.protocol.server

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.Count
import io.github.projectpidove.showdown.battle.{*, given}
import io.github.projectpidove.showdown.protocol.{MessageDecoder, ProtocolError, messageName, messagePrefix}
import io.github.projectpidove.showdown.room.{ChatContent, given}
import io.github.projectpidove.showdown.team.*

@messagePrefix("-")
enum BattleAttackMessage derives MessageDecoder:

  /**
   * A move failed.
   *
   * @param pokemon the move user
   * @param move    the failed move
   */
  case Fail(pokemon: ActiveId, move: MoveName)

  /**
   * An effect blocked a move.
   *
   * @param pokemon  the pokemon who blocked the move
   * @param effect   the blocking effect
   * @param attacker the blocked pokemon
   */
  case Block(pokemon: ActiveId, effect: Effect, attacker: ActiveId)

  /**
   * A pokemon does not have any target.
   *
   * @param pokemon the pokemon who tried to attack
   */
  case NoTarget(pokemon: ActiveId)

  /**
   * A pokemon avoided an attack
   *
   * @param pokemon the move user
   * @param target  the pokemon who avoided the attack
   */
  case Miss(pokemon: ActiveId, target: ActiveId)

  /**
   * A pokemon made a critical hit.
   */
  @messageName("crit") case CriticalHit

  /**
   * A move was super effective.
   *
   * @param pokemon the defender
   */
  case SuperEffective(pokemon: ActiveId)

  /**
   * A move was not very effective.
   *
   * @param pokemon the defender
   */
  case Resisted(pokemon: ActiveId)

  /**
   * A pokemon is immune to the used move.
   *
   * @param pokemon the defender
   */
  case Immune(pokemon: ActiveId)

  /**
   * The move has been combined with another (e.g Fire Pledge).
   */
  case Combine

  /**
   * A move user is waiting for the target.
   *
   * @param pokemon the move user
   * @param target  the waited-for pokemon
   */
  case Waiting(pokemon: ActiveId, target: ActiveId)

  /**
   * A pokemon is charging a move on a potentially unknown target.
   *
   * @param pokemon  the attacking pokemon
   * @param move     the charging move
   * @param defender the move target if known
   */
  case Prepare(pokemon: ActiveId, move: MoveName, defender: Option[ActiveId])

  /**
   * A pokemon must recharge (e.g after using Hyper Beam).
   *
   * @param pokemon the pokemon needing to recharge
   */
  case MustRecharge(pokemon: ActiveId)

  /**
   * Deprecated message to indicate that nothing happened.
   */
  @messageName("nothing") case NothingHappened

  /**
   * A pokemon hit n times.
   *
   * @param pokemon the attacker
   * @param count   the number of time the pokemon hit
   */
  case HitCount(pokemon: ActiveId, count: Count)