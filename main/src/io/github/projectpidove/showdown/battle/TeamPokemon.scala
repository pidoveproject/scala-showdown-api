package io.github.projectpidove.showdown.battle

case class TeamPokemon(details: PokemonDetails, condition: Condition):

  def withHealth(health: Health): TeamPokemon = this.copy(condition = condition.copy(health = health))

  def withStatus(status: StatusEffect): TeamPokemon = this.copy(condition = condition.copy(status = Some(status)))

  def cured: TeamPokemon = this.copy(condition = condition.copy(status = None))