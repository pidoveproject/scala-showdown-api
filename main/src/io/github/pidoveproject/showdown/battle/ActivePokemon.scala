package io.github.pidoveproject.showdown.battle

import io.github.iltotore.iron.*
import io.github.pidoveproject.showdown.team.{AbilityName, SpeciesName, StatType}

import scala.math.Integral.Implicits.infixIntegralOps

/**
 * An pokemon active on the battlefield.
 *
 * @param teamSlot the slot of this pokemon in its team, used to retrieve switch-persistent information like health
 * @param boosts the current boosts of the pokemon
 * @param volatileStatus the current volatile (not persistent after switching) status of the pokemon
 * @param nextMoveStatus the status ending the next time this pokemon uses a move
 * @param nextTurnStatus the status ending the next turn
 * @param modifiedAbility the new ability of the pokemon, `None` indicates that it still has its base ability
 * @param transformedSpecies the new species of the pokemon, `None` indicates that it still has its base species
 */
case class ActivePokemon(
    teamSlot: TeamSlot,
    boosts: Map[StatType, StatBoost] = Map.empty,
    volatileStatus: Set[VolatileStatus] = Set.empty,
    nextMoveStatus: Set[VolatileStatus] = Set.empty,
    nextTurnStatus: Set[VolatileStatus] = Set.empty,
    modifiedAbility: Option[RevealedAbility] = None,
    transformedSpecies: Option[SpeciesName] = None,
    isTerastallized: Boolean = false
):

  /**
   * Get the boost of this pokemon in a stat.
   *
   * @param stat the stat to get boost from
   * @return the boost (negative means de-buff) of this pokemon in the given stat
   */
  def getBoost(stat: StatType): Option[StatBoost] = boosts.get(stat)

  /**
   * Set the boost to this pokemon in a stat.
   *
   * @param stat the stat to boost
   * @param boost the boost to apply to the stat
   * @return a copy of this pokemon with the given boost applied
   */
  def withBoost(stat: StatType, boost: StatBoost): ActivePokemon = this.copy(boosts = boosts.updated(stat, boost))

  /**
   * Boost the given stat.
   *
   * @param stat the stat to boost
   * @param boost the level of the boost
   * @return a copy of this pokemon with the given boost added to the stat
   */
  def boosted(stat: StatType, boost: StatBoost): ActivePokemon = this.withBoost(stat, getBoost(stat).getOrElse(StatBoost(0)) + boost)

  /**
   * Clear all boosts.
   *
   * @return a copy of this pokemon without any boost
   */
  def boostsCleared: ActivePokemon = this.copy(boosts = Map.empty)

  /**
   * Add a volatile status to this pokemon
   *
   * @param status the status to add
   * @return a copy of this pokemon with the given status
   */
  def withVolatileStatus(status: VolatileStatus): ActivePokemon = this.copy(volatileStatus = volatileStatus + status)

  /**
   * Remove a volatile status to this pokemon
   *
   * @param status the status to remove
   * @return a copy of this pokemon without the given status
   */
  def removedVolatileStatus(status: VolatileStatus): ActivePokemon = this.copy(volatileStatus = volatileStatus - status)

  /**
   * Add a status to this pokemon that will end the next time this pokemon uses a move.
   *
   * @param status the status to add
   * @return a copy of this pokemon with the given status
   */
  def withNextMoveStatus(status: VolatileStatus): ActivePokemon = this.copy(nextMoveStatus = nextMoveStatus + status)

  /**
   * Add a status to this pokemon that will end the next turn.
   *
   * @param status the status to add
   * @return a copy of this pokemon with the given status
   */
  def withNextTurnStatus(status: VolatileStatus): ActivePokemon = this.copy(nextTurnStatus = nextTurnStatus + status)

  /**
   * Remove all status that should end the next time this pokemon uses a move.
   *
   * @return a copy of this pokemon without any "next move status"
   */
  def clearNextMoveStatus: ActivePokemon = this.copy(nextMoveStatus = Set.empty)

  /**
   * Change the ability of this pokemon.
   *
   * @param ability the new ability to give
   * @param cause the cause of the ability change
   * @return a copy of this pokemon with the given ability
   */
  def withModifiedAbility(ability: AbilityName, cause: Effect): ActivePokemon =
    this.copy(modifiedAbility = Some(RevealedAbility.Modified(ability, cause)))

  /**
   * Disable the ability of this pokemon.
   *
   * @return a copy of this pokemon with the ability [[RevealedAbility.Disabled]]
   */
  def disabledAbility: ActivePokemon = this.copy(modifiedAbility = Some(RevealedAbility.Disabled))

  def terastallized: ActivePokemon = this.copy(isTerastallized = true)
