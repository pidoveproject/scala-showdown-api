package io.github.projectpidove.showdown.battle

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.team.StatType
import scala.math.Integral.Implicits.infixIntegralOps

case class ActivePokemon(
    teamPokemon: TeamPokemon,
    boosts: Map[StatType, StatBoost] = Map.empty,
    volatileStatus: Set[VolatileStatus] = Set.empty,
    nextMoveStatus: Set[VolatileStatus] = Set.empty,
    nextTurnStatus: Set[VolatileStatus] = Set.empty
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

object ActivePokemon:

  def switchedIn(details: PokemonDetails, condition: Condition): ActivePokemon = ActivePokemon(TeamPokemon(details, condition))
