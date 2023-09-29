package io.github.projectpidove.showdown.battle

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.team.{AbilityName, SpeciesName, StatType}

import scala.math.Integral.Implicits.infixIntegralOps

case class ActivePokemon(
                          teamSlot: TeamSlot,
                          boosts: Map[StatType, StatBoost] = Map.empty,
                          volatileStatus: Set[VolatileStatus] = Set.empty,
                          nextMoveStatus: Set[VolatileStatus] = Set.empty,
                          nextTurnStatus: Set[VolatileStatus] = Set.empty,
                          modifiedAbility: Option[RevealedAbility] = None,
                          transformedSpecies: Option[SpeciesName] = None
):

  def getBoost(stat: StatType): Option[StatBoost] = boosts.get(stat)

  def withBoost(stat: StatType, boost: StatBoost): ActivePokemon = this.copy(boosts = boosts.updated(stat, boost))

  def boosted(stat: StatType, boost: StatBoost): ActivePokemon = this.withBoost(stat, getBoost(stat).getOrElse(StatBoost(0)) + boost)

  def boostsCleared: ActivePokemon = this.copy(boosts = Map.empty)

  def withVolatileStatus(status: VolatileStatus): ActivePokemon = this.copy(volatileStatus = volatileStatus + status)

  def removedVolatileStatus(status: VolatileStatus): ActivePokemon = this.copy(volatileStatus = volatileStatus - status)
  
  def withNextMoveStatus(status: VolatileStatus): ActivePokemon = this.copy(nextMoveStatus = nextMoveStatus + status)
  
  def withNextTurnStatus(status: VolatileStatus): ActivePokemon = this.copy(nextTurnStatus = nextTurnStatus + status)

  def clearMoveStatus: ActivePokemon = this.copy(nextMoveStatus = Set.empty)

  def withModifiedAbility(ability: AbilityName, cause: Effect): ActivePokemon = this.copy(modifiedAbility = Some(RevealedAbility.Modified(ability, cause)))

  def disabledAbility: ActivePokemon = this.copy(modifiedAbility = Some(RevealedAbility.Disabled))
