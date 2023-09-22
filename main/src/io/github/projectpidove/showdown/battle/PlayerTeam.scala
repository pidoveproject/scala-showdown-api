package io.github.projectpidove.showdown.battle

import io.github.projectpidove.showdown.Count
import io.github.projectpidove.showdown.protocol.server.{BattleInitializationMessage, BattleMessage}

case class PlayerTeam(size: Count, members: Map[TeamSlot, TeamPokemon] = Map.empty):

  def withPokemon(slot: TeamSlot, pokemon: TeamPokemon): PlayerTeam = this.copy(members = members.updated(slot, pokemon))