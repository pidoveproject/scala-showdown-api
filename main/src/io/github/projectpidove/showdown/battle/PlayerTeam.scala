package io.github.projectpidove.showdown.battle

import io.github.projectpidove.showdown.Count
import io.github.projectpidove.showdown.protocol.server.{BattleInitializationMessage, BattleMessage}
import io.github.projectpidove.showdown.team.Surname

case class PlayerTeam(size: Count, members: Map[TeamSlot, TeamPokemon] = Map.empty):

  def withPokemon(slot: TeamSlot, pokemon: TeamPokemon): PlayerTeam = this.copy(members = members.updated(slot, pokemon))

  def getPokemon(slot: TeamSlot): Option[TeamPokemon] = members.get(slot)

  def replacedDetails(slot: TeamSlot, details: PokemonDetails): PlayerTeam =
    this.copy(members = members.updatedWith(slot)(_.map(_.copy(details = details))))

  def getSlotByDetails(details: PokemonDetails): Option[TeamSlot] =
    members.collectFirst:
      case (slot, pokemon) if pokemon.details == details => slot

  def firstAvailableSlot: Option[TeamSlot] =
    Range.inclusive(1, size.value).map(TeamSlot.assume(_)).collectFirst:
      case slot if !members.contains(slot) => slot

