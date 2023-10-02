package io.github.projectpidove.showdown.protocol.server

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.battle.{*, given}
import io.github.projectpidove.showdown.protocol.{MessageDecoder, messageName}
import io.github.projectpidove.showdown.team.MoveName

/**
 * A major action on an active battle.
 */
enum BattleMajorActionMessage derives MessageDecoder:

  /**
   * A pokemon used a move.
   *
   * @param pokemon the attacker
   * @param move the move used by the attacker
   * @param target the pokemon taking the move
   */
  case Move(pokemon: ActiveId, move: MoveName, target: ActiveId)

  /**
   * A pokemon switched-in.
   *
   * @param pokemon the switched-in pokemon
   * @param details further details about the switched-in pokemon
   * @param condition the health and current status of the switched-in pokemon
   * @param cause the optional cause of the switch-in (e.g U-turn, Dragon Tail...)
   */
  @messageName("switch", "drag") case Switch(pokemon: ActiveId, details: PokemonDetails, condition: Condition, cause: Option[Effect])

  /**
   * Details about a pokemon changed. Happens when the pokemon changes form.
   *
   * @param pokemon the modified pokemon
   * @param details the new details of the pokemon
   * @param condition the health and current status of the pokemon
   */
  @messageName("detailschange", "-formechange") case DetailsChange(pokemon: ActiveId, details: PokemonDetails, condition: Option[Condition])

  /**
   * Replace pokemon information. Sent when Zoroark's illusion fades.
   *
   * @param pokemon      the new pokemon
   * @param details      further details about the new pokemon
   * @param condition the health and current status of the switched-in pokemon
   */
  case Replace(pokemon: ActiveId, details: PokemonDetails, condition: Condition)

  /**
   * Change the position of a pokemon.
   *
   * @param pokemon the pokemon to swap
   * @param slot the slot to put the pokemon in
   */
  case Swap(pokemon: ActiveId, slot: PokemonSlot)

  /**
   * A pokemon is unable to do an action.
   *
   * @param pokemon the pokemon unable to execute its action
   * @param reason the reason of the incapacity
   * @param move the optional move causing the incapacity
   */
  @messageName("cant") case Unable(pokemon: ActiveId, reason: String, move: Option[MoveName])

  /**
   * A pokemon fainted.
   *
   * @param pokemon the fainted pokemon
   */
  case Faint(pokemon: ActiveId)