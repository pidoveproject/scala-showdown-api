package io.github.projectpidove.showdown.battle

import io.github.projectpidove.showdown.team.{AbilityName, ItemName}

case class TeamPokemon(
  details: PokemonDetails,
  condition: Condition,
  item: HeldItem = HeldItem.Unknown,
  ability: Option[AbilityName] = None
):

  def withHealth(health: Health): TeamPokemon = this.copy(condition = condition.copy(health = health))

  def withStatus(status: StatusEffect): TeamPokemon = this.copy(condition = condition.copy(status = Some(status)))

  def cured: TeamPokemon = this.copy(condition = condition.copy(status = None))

  def revealedItem(item: ItemName, cause: Option[Effect]): TeamPokemon = this.copy(item = HeldItem.Revealed(item, cause))

  def destroyedItem(item: ItemName, cause: Option[Effect]): TeamPokemon = this.copy(item = HeldItem.Destroyed(item, cause))

  def revealedAbility(ability: AbilityName): TeamPokemon =
    this.copy(ability = Some(ability))