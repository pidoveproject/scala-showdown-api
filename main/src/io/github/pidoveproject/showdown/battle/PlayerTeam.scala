package io.github.pidoveproject.showdown.battle

import io.github.pidoveproject.showdown.Count

/**
 * The pokemon team of a player.
 *
 * @param size the size of the team
 * @param members the pokemon members of the team
 */
case class PlayerTeam(size: Count, members: Map[TeamSlot, TeamMember] = Map.empty):

  /**
   * Set the member at the given slot.
   *
   * @param slot the slot to modify
   * @param pokemon the pokemon to set
   * @return a copy of this team with the given pokemon added
   */
  def withPokemon(slot: TeamSlot, pokemon: TeamMember): PlayerTeam = this.copy(members = members.updated(slot, pokemon))

  /**
   * Get the pokemon at the given slot.
   *
   * @param slot the slot to get from
   * @return the pokemon at `slot`
   */
  def getPokemon(slot: TeamSlot): Option[TeamMember] = members.get(slot)

  /**
   * Replace the details of a team member.
   *
   * @param slot the position of the pokemon to modify
   * @param details the new details of the pokemon
   * @return a copy of this team with the given details
   */
  def replaceDetails(slot: TeamSlot, details: PokemonDetails): PlayerTeam =
    this.copy(members = members.updatedWith(slot)(_.map(_.copy(details = details))))

  /**
   * Retrieve the slot of a pokemon using its details.
   *
   * @param details the details to identify the pokemon
   * @return the matching pokemon's slot or [[None]]
   */
  def getSlotByDetails(details: PokemonDetails): Option[TeamSlot] =
    members.collectFirst:
      case (slot, pokemon) if pokemon.details ~= details => slot

  /**
   * Get the first available team slot
   */
  def firstAvailableSlot: Option[TeamSlot] =
    Range.inclusive(1, size.value).map(TeamSlot.assume(_)).collectFirst:
      case slot if !members.contains(slot) => slot

