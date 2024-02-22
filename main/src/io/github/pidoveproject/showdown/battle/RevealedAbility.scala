package io.github.pidoveproject.showdown.battle

import io.github.pidoveproject.showdown.team.AbilityName

/**
 * A revealed ability.
 */
enum RevealedAbility:

  /**
   * The base ability of a pokemon.
   * 
   * @param ability the ability of the pokemon
   */
  case Base(ability: AbilityName)

  /**
   * A modified ability.
   * 
   * @param ability the current ability of the pokemon 
   * @param cause the effect causing the ability change
   */
  case Modified(ability: AbilityName, cause: Effect)

  /**
   * The pokemon's ability is currently disabled (e.g by Neutralising Gas)
   */
  case Disabled