package io.github.projectpidove.showdown.battle

import io.github.projectpidove.showdown.team.StatType

case class ActivePokemon(
  teamPokemon: TeamPokemon,
  boosts: Map[StatType, StatBoost] = Map.empty,
  volatileStatus: Set[VolatileStatus] = Set.empty,
  nextMoveStatus: Set[VolatileStatus] = Set.empty
):
  
  def getBoost(statType: StatType): Option[StatBoost] = boosts.get(statType)
  
  def setBoost(statType: StatType, boost: StatBoost): ActivePokemon = this.copy(boosts = boosts.updated(statType, boost))

  def withNextMoveStatus(status: VolatileStatus): ActivePokemon = this.copy(nextMoveStatus = nextMoveStatus + status)
  
  def clearMoveStatus: ActivePokemon = this.copy(nextMoveStatus = Set.empty)
  
object ActivePokemon:
  
  def switchedIn(details: PokemonDetails, condition: Condition): ActivePokemon = ActivePokemon(TeamPokemon(details, condition))