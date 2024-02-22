package io.github.pidoveproject.showdown.battle

import io.github.pidoveproject.showdown.team.{AbilityName, ItemName}

/**
 * An inactive pokemon.
 *
 * @param details the known details of this pokemon
 * @param condition the condition of this pokemon
 * @param item the item currently held by this pokemon
 * @param ability the ability of this pokemon
 */
case class TeamMember(
  details: PokemonDetails,
  condition: Condition = Condition.Healthy,
  item: HeldItem = HeldItem.Unknown,
  ability: Option[AbilityName] = None
):

  /**
   * Set the health of this pokemon.
   *
   * @param health the new health value
   * @return a copy of this pokemon with the given health
   */
  def withHealth(health: Health): TeamMember = this.copy(condition = condition.copy(health = health))

  /**
   * Set the status of this pokemon.
   *
   * @param status the new status
   * @return a copy of this pokemon with the given status
   */
  def withStatus(status: StatusEffect): TeamMember = this.copy(condition = condition.copy(status = Some(status)))

  /**
   * Remove status from this pokemon. A fainted pokemon will stay as is.
   */
  def cured: TeamMember =
    if !condition.fainted then this.copy(condition = condition.copy(status = None))
    else this

  /**
   * Reveal the item of this pokemon.
   *
   * @param item the revealed held item
   * @param cause the cause of the reveal
   * @return a copy of this pokemon with the revealed item
   */
  def withRevealedItem(item: ItemName, cause: Option[Effect]): TeamMember = this.copy(item = HeldItem.Revealed(item, cause))

  /**
   * Destroy (and reveal) the item of this pokemon.
   *
   * @param item  the destroyed held item
   * @param cause the cause of the destruction
   * @return a copy of this pokemon with the destroyed item
   */
  def withDestroyedItem(item: ItemName, cause: Option[Effect]): TeamMember = this.copy(item = HeldItem.Destroyed(item, cause))

  /**
   * Reveal the ability of this pokemon.
   *
   * @param ability the revealed ability
   * @return a copy of this pokemon with the revealed ability
   */
  def withRevealedAbility(ability: AbilityName): TeamMember =
    this.copy(ability = Some(ability))