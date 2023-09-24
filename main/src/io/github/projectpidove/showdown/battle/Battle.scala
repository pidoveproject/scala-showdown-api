package io.github.projectpidove.showdown.battle

import io.github.projectpidove.showdown.protocol.server.choice.ChoiceRequest
import io.github.projectpidove.showdown.{FormatName, Generation}
import io.github.projectpidove.showdown.protocol.server.{BattleAttackMessage, BattleInitializationMessage, BattleMajorActionMessage, BattleMessage, BattleProgressMessage}
import io.github.projectpidove.showdown.user.Username

case class Battle(
    state: BattleState,
    battleType: Option[BattleType],
    players: Map[PlayerNumber, Player],
    generation: Option[Generation],
    format: Option[FormatName],
    rules: Set[BattleRule],
    timerEnabled: Boolean,
    currentTurn: Option[TurnNumber],
    result: Option[BattleResult],
    currentRequest: Option[ChoiceRequest],
    activePokemon: Map[PokemonId, ActivePokemon]
):

  def updatePlayer(number: PlayerNumber, f: Player => Player): Battle =
    players.get(number).map(f).fold(this)(player => this.copy(players = players.updated(number, player)))

  def getActivePokemon(id: PokemonId): Option[ActivePokemon] = activePokemon.get(id)

  def withActivePokemon(id: PokemonId, pokemon: ActivePokemon): Battle =
    this.copy(activePokemon = activePokemon.updated(id, pokemon))

  def updateActivePokemon(id: PokemonId, f: ActivePokemon => ActivePokemon): Battle =
    this.copy(activePokemon = activePokemon.updatedWith(id)(_.map(f)))

  def replacePokemonAt(position: PokemonPosition, newId: PokemonId, f: ActivePokemon => ActivePokemon): Battle =
    activePokemon.find(_._1.position == position) match
      case Some((oldId, pokemon)) => this.copy(activePokemon = activePokemon.removed(oldId).updated(newId, f(pokemon)))
      case None => this

  def changePosition(id: PokemonId, slot: PokemonSlot): Battle =
    getActivePokemon(id) match
      case Some(pokemon) =>
        val newId = id.copy(position = id.position.copy(slot = slot))
        this.copy(activePokemon = activePokemon.removed(id).updated(newId, pokemon))

      case None => this

  def update(message: BattleMessage): Battle = message match
    case BattleInitializationMessage.Player(number, name, avatar, rating) =>
      this.copy(players = players.updated(number, Player(number, name, avatar, rating)))

    case BattleInitializationMessage.TeamSize(player, size) =>
      this.updatePlayer(player, _.copy(team = Some(PlayerTeam(size))))

    case BattleInitializationMessage.GameType(battleType) => this.copy(battleType = Some(battleType))
    case BattleInitializationMessage.Gen(generation) => this.copy(generation = Some(generation))
    case BattleInitializationMessage.Tier(format) => this.copy(format = Some(format))
    case BattleInitializationMessage.Rule(rule) => this.copy(rules = rules + rule)
    case BattleInitializationMessage.StartPreview() => this.copy(state = BattleState.Preview)
    case BattleInitializationMessage.Start() => this.copy(state = BattleState.Playing)
    case BattleProgressMessage.TimerMessage(_) => this.copy(timerEnabled = true)
    case BattleProgressMessage.TimerDisabled(_) => this.copy(timerEnabled = false)
    case BattleProgressMessage.Turn(turn) => this.copy(currentTurn = Some(turn))
    case BattleProgressMessage.Win(user) => this.copy(result = Some(BattleResult.Win(user)))
    case BattleProgressMessage.Tie() => this.copy(result = Some(BattleResult.Tie))
    case BattleMajorActionMessage.Switch(pokemon, details, healthStatus) =>
      this.withActivePokemon(pokemon, ActivePokemon.switchedIn(details, healthStatus))
    case BattleMajorActionMessage.DetailsChange(pokemon, details, healthStatus) =>
      this.updateActivePokemon(pokemon, p => p.copy(teamPokemon = TeamPokemon(p.teamPokemon.details.merge(details), healthStatus.getOrElse(p.teamPokemon.condition))))
    case BattleMajorActionMessage.Replace(pokemon, details, healthStatus) =>
      this.updateActivePokemon(pokemon, p => p.copy(teamPokemon = TeamPokemon(details, healthStatus)))
    case BattleMajorActionMessage.Swap(pokemon, slot) =>
      this.changePosition(pokemon, slot)
    case BattleMajorActionMessage.Faint(pokemon) =>
      this.updateActivePokemon(pokemon, p => p.copy(teamPokemon = p.teamPokemon.copy(condition = p.teamPokemon.condition.faint)))

    case _ => this

object Battle:

  val empty: Battle = Battle(
    state = BattleState.Initialization,
    battleType = None,
    players = Map.empty,
    generation = None,
    format = None,
    rules = Set.empty,
    timerEnabled = true,
    currentTurn = None,
    result = None,
    currentRequest = None,
    activePokemon = Map.empty
  )