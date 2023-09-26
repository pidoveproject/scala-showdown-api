package io.github.projectpidove.showdown.battle

import io.github.projectpidove.showdown.user.{AvatarName, Username}

case class Player(
     number: PlayerNumber,
     name: Username,
     avatar: AvatarName,
     rating: Rating,
     activePokemon: Map[PokemonId, ActivePokemon] = Map.empty,
     teamSnapshot: Option[PlayerTeam] = None
):

  def currentTeam: Option[PlayerTeam] = teamSnapshot.map: baseTeam =>
    activePokemon.foldLeft(baseTeam)((team, tpl) => tpl._2.teamSlot.fold(team)(slot => team.withPokemon(slot, tpl._2.teamPokemon)))

  def guessSlot(details: PokemonDetails): Option[TeamSlot] = teamSnapshot.flatMap: teams =>
    teams.members.collectFirst:
      case (slot, pokemon) if pokemon.details == details => slot