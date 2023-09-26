package io.github.projectpidove.showdown.battle

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.team.{AbilityName, SpeciesName, StatType}

import scala.math.Integral.Implicits.infixIntegralOps

case class ActivePokemon(
    teamPokemon: TeamPokemon,
    teamSlot: Option[TeamSlot] = None,
    boosts: Map[StatType, StatBoost] = Map.empty,
    volatileStatus: Set[VolatileStatus] = Set.empty,
    nextMoveStatus: Set[VolatileStatus] = Set.empty,
    nextTurnStatus: Set[VolatileStatus] = Set.empty,
    modifiedAbility: Option[CurrentAbility] = None,
    transformedSpecies: Option[SpeciesName] = None
):

  def currentAbility: Option[CurrentAbility] =
    modifiedAbility.orElse(teamPokemon.ability.map(CurrentAbility.LongTerm.apply))

  def currentSpecies: SpeciesName = transformedSpecies.getOrElse(teamPokemon.details.species)

  def getBoost(stat: StatType): Option[StatBoost] = boosts.get(stat)

  def withBoost(stat: StatType, boost: StatBoost): ActivePokemon = this.copy(boosts = boosts.updated(stat, boost))

  def boosted(stat: StatType, boost: StatBoost): ActivePokemon = this.withBoost(stat, getBoost(stat).getOrElse(StatBoost(0)) + boost)

  def boostsCleared: ActivePokemon = this.copy(boosts = Map.empty)

  def withVolatileStatus(status: VolatileStatus): ActivePokemon = this.copy(volatileStatus = volatileStatus + status)

  def removedVolatileStatus(status: VolatileStatus): ActivePokemon = this.copy(volatileStatus = volatileStatus - status)
  
  def withNextMoveStatus(status: VolatileStatus): ActivePokemon = this.copy(nextMoveStatus = nextMoveStatus + status)
  
  def withNextTurnStatus(status: VolatileStatus): ActivePokemon = this.copy(nextTurnStatus = nextTurnStatus + status)

  def clearMoveStatus: ActivePokemon = this.copy(nextMoveStatus = Set.empty)

  def withModifiedAbility(ability: AbilityName, cause: Effect): ActivePokemon = this.copy(modifiedAbility = Some(CurrentAbility.Modified(ability, cause)))

  def disabledAbility: ActivePokemon = this.copy(modifiedAbility = Some(CurrentAbility.Disabled))

  def transformedInto(target: ActivePokemon): ActivePokemon =
    this.copy(
      boosts = target.boosts,
      modifiedAbility = target.currentAbility,
      transformedSpecies = Some(target.currentSpecies)
    )

object ActivePokemon:

  def switchedIn(details: PokemonDetails, condition: Condition): ActivePokemon =
    ActivePokemon(TeamPokemon(details, condition))
