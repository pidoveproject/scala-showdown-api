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
  case Move(pokemon: PokemonId, move: MoveName, target: PokemonId)

  /**
   * A pokemon switched-in/out.
   *
   * @param pokemon the switched-in pokemon
   * @param details further details about the switched-in pokemon
   * @param healthStatus the health and current status of the switched-in pokemon
   */
  @messageName("switch", "drag") case Switch(pokemon: PokemonId, details: PokemonDetails, healthStatus: HealthStatus)

  /**
   * Details about a pokemon changed. Happens when the pokemon changes form.
   *
   * @param pokemon the modified pokemon
   * @param details the new details of the pokemon
   * @param healthStatus the health and current status of the pokemon
   */
  @messageName("detailschange", "-formechange") case DetailsChange(pokemon: PokemonId, details: PokemonDetails, healthStatus: Option[HealthStatus])

  /**
   * Replace pokemon information. Sent when Zoroark's illusion fades.
   *
   * @param pokemon      the new pokemon
   * @param details      further details about the new pokemon
   * @param healthStatus the health and current status of the switched-in pokemon
   */
  case Replace(pokemon: PokemonId, details: PokemonDetails, healthStatus: HealthStatus)

  /**
   * Change the position of a pokemon.
   *
   * @param pokemon the pokemon to swap
   * @param slot the slot to put the pokemon in
   */
  case Swap(pokemon: PokemonId, slot: PokemonSlot)

  /**
   * A pokemon is unable to do an action.
   *
   * @param pokemon the pokemon unable to execute its action
   * @param reason the reason of the incapacity
   * @param move the optional move causing the incapacity
   */
  @messageName("cant") case Unable(pokemon: PokemonId, reason: String, move: Option[MoveName])

  /**
   * A pokemon fainted.
   *
   * @param pokemon the fainted pokemon
   */
  case Faint(pokemon: PokemonId)