package io.github.projectpidove.showdown.client

import io.github.projectpidove.showdown.battle.*
import io.github.projectpidove.showdown.protocol.server.choice.*

def showTeamMember(member: TeamMember): String =
  val species = member.details.species
  val item = member.item match
    case HeldItem.Revealed(item, Some(cause)) => s"@ $item (Revealed by $cause})"
    case HeldItem.Revealed(item, None) => s"@ $item"
    case HeldItem.Destroyed(item, Some(cause)) => s"@ $item (Destroyed by $cause})"
    case HeldItem.Destroyed(item, None) => s"@ $item"
    case HeldItem.Unknown => ""

  val condition = member.condition match
    case Condition(Health(current, max), Some(status)) => s"$current/$max $status"
    case Condition(Health(current, max), None) => s"$current/$max"

  val ability = member.ability.fold("Unknown ability")(_.value)

  s"$species $item, $ability, $condition"

def showTeam(player: PlayerNumber, team: PlayerTeam): String =
  val memberInfo =
    team
      .members
      .map((slot, member) => s"$slot: ${showTeamMember(member)}")

  val remainingLines = Range(team.members.size, team.size.value).map(slot => s"$slot:")

  s"""=== Player $player ===
     |${(memberInfo ++ remainingLines).mkString("\n")}""".stripMargin

def showActive(position: ActivePosition, pokemon: ActivePokemon, member: TeamMember): String =
  val species = pokemon.transformedSpecies match
    case Some(transformed) => s"$transformed (${member.details.species})"
    case None => member.details.species

  val item = member.item match
    case HeldItem.Revealed(item, Some(cause)) => s"@ $item (Revealed by $cause})"
    case HeldItem.Revealed(item, None) => s"@ $item"
    case HeldItem.Destroyed(item, Some(cause)) => s"@ $item (Destroyed by $cause})"
    case HeldItem.Destroyed(item, None) => s"@ $item"
    case HeldItem.Unknown => ""

  val condition = member.condition match
    case Condition(Health(current, max), Some(status)) => s"$current/$max $status"
    case Condition(Health(current, max), None) => s"$current/$max"

  val baseAbility = member.ability.fold("Unknown Ability")(_.value)

  val ability = pokemon.modifiedAbility match
    case Some(RevealedAbility.Base(ability)) => ability
    case Some(RevealedAbility.Modified(ability, _)) => s"$ability ($baseAbility)"
    case Some(RevealedAbility.Disabled) => s"Disabled ($baseAbility)"
    case None => baseAbility

  s"""=== $position ===
     |$species $item
     |Condition: $condition
     |Ability: $ability""".stripMargin


def showAllActive(battle: Battle): String =
  val entries =
    battle.activePokemon.map: (position, pokemon) =>
      battle.getTeamMemberAt(position) match
        case Some(value) => showActive(position, pokemon, value)
        case None =>
          s"""=== $position ===
             |???""".stripMargin

  entries.mkString("\n")

def showMoveChoice(choice: MoveChoice): String = s"${choice.name} (${choice.pp}/${choice.maxPP})"

def showPokemonChoice(choice: PokemonChoice): String =
  val condition = choice.condition match
    case Condition(Health(current, max), Some(status)) => s"$current/$max $status"
    case Condition(Health(current, max), None) => s"$current/$max"

  s"${choice.details.species} ($condition)"

def showActiveChoice(choice: ActiveChoice): String =
  choice.moves.map(showMoveChoice).mkString(" / ")

def showTeamChoice(choice: TeamChoice): String =
  choice.pokemon.map(showPokemonChoice).mkString(" / ")

def showChoiceRequest(choice: ChoiceRequest): String =
  val activeChoices = choice.active.map(showActiveChoice).mkString("\n")

  s"""Moves:
     |$activeChoices
     |
     |Switch:
     |${showTeamChoice(choice.team)}""".stripMargin