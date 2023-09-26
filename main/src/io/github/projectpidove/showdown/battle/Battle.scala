package io.github.projectpidove.showdown.battle

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.protocol.server.choice.ChoiceRequest
import io.github.projectpidove.showdown.{FormatName, Generation}
import io.github.projectpidove.showdown.protocol.server.{BattleAttackMessage, BattleInitializationMessage, BattleMajorActionMessage, BattleMessage, BattleMinorActionMessage, BattleProgressMessage, BattleStatusMessage}
import io.github.projectpidove.showdown.user.Username

import scala.math.Integral.Implicits.infixIntegralOps
import scala.math.Ordering.Implicits.infixOrderingOps

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
    weather: Option[Weather],
    field: Map[FieldEffect, TurnNumber],
    sides: Map[PlayerPosition, SideCondition]
):

  def updatePlayer(number: PlayerNumber, f: Player => Player): Battle =
    players.get(number).map(f).fold(this)(player => this.copy(players = players.updated(number, player)))

  def getActivePokemon(id: PokemonId): Option[ActivePokemon] =
    players
      .get(id.position.player)
      .flatMap(_.activePokemon.get(id))

  def withActivePokemon(id: PokemonId, pokemon: ActivePokemon): Battle =
    players.get(id.position.player) match
      case Some(player) =>
        val updated = player.copy(activePokemon = player.activePokemon.updated(id, pokemon))
        this.copy(players = players.updated(id.position.player, updated))
      case None => this

  def updateActivePokemon(id: PokemonId)(f: ActivePokemon => ActivePokemon): Battle =
    players.get(id.position.player) match
      case Some(player) =>
        val updated = player.copy(activePokemon = player.activePokemon.updatedWith(id)(_.map(f)))
        this.copy(players = players.updated(id.position.player, updated))
      case None => this

  def updateAllActivePokemon(f: ActivePokemon => ActivePokemon): Battle =
    val updatedPlayers =
      players.map: (k, player) =>
        (k, player.copy(activePokemon = player.activePokemon.map((k, v) => (k, f(v)))))

    this.copy(players = updatedPlayers)

  def replacePokemonAt(position: PokemonPosition, newId: PokemonId, f: ActivePokemon => ActivePokemon): Battle =
    val updated =
      for
        player <- players.get(position.player)
        (oldId, pokemon) <- player.activePokemon.find(_._1.position == position)
      yield
        val updatedActivePokemon =
          player
            .activePokemon
            .removed(oldId)
            .updated(newId, pokemon)

        val updatedPlayer = player.copy(activePokemon = updatedActivePokemon)
        this.copy(players = players.updated(position.player, player))

    updated.getOrElse(this)

  def changePosition(id: PokemonId, slot: PokemonSlot): Battle =
    val updated =
      for
        player <- players.get(id.position.player)
        pokemon <- player.activePokemon.get(id)
      yield
        val updatedActivePokemon =
          player
            .activePokemon
            .removed(id)
            .updated(id.copy(position = id.position.copy(slot = slot)), pokemon)

        val updatedPlayer = player.copy(activePokemon = updatedActivePokemon)
        this.copy(players = players.updated(id.position.player, player))

    updated.getOrElse(this)

  def updateSide(side: PlayerPosition)(f: SideCondition => SideCondition): Battle =
    this.copy(sides = sides.updatedWith(side)(_.map(f)))

  def update(message: BattleMessage): Battle = message match

    //Initialization
    case BattleInitializationMessage.Player(number, name, avatar, rating) =>
      this.copy(players = players.updated(number, Player(number, name, avatar, rating)))
    case BattleInitializationMessage.TeamSize(player, size) =>
      this.updatePlayer(player, _.copy(teamSnapshot = Some(PlayerTeam(size))))
    case BattleInitializationMessage.GameType(battleType) => this.copy(battleType = Some(battleType))
    case BattleInitializationMessage.Gen(generation) => this.copy(generation = Some(generation))
    case BattleInitializationMessage.Tier(format) => this.copy(format = Some(format))
    case BattleInitializationMessage.Rule(rule) => this.copy(rules = rules + rule)
    case BattleInitializationMessage.StartPreview() => this.copy(state = BattleState.Preview)
    case BattleInitializationMessage.Start() => this.copy(state = BattleState.Playing)

    //Progress
    case BattleProgressMessage.TimerMessage(_) => this.copy(timerEnabled = true)
    case BattleProgressMessage.TimerDisabled(_) => this.copy(timerEnabled = false)
    case BattleProgressMessage.Turn(turn) => this.copy(currentTurn = Some(turn))
    case BattleProgressMessage.Win(user) => this.copy(result = Some(BattleResult.Win(user)))
    case BattleProgressMessage.Tie() => this.copy(result = Some(BattleResult.Tie))

    //Major action
    case BattleMajorActionMessage.Switch(pokemon, details, healthStatus) =>
      this.withActivePokemon(pokemon, ActivePokemon.switchedIn(details, healthStatus))
    case BattleMajorActionMessage.DetailsChange(pokemon, details, healthStatus) =>
      this.updateActivePokemon(pokemon): p =>
        p.copy(teamPokemon = TeamPokemon(p.teamPokemon.details.merge(details), healthStatus.getOrElse(p.teamPokemon.condition)))
    case BattleMajorActionMessage.Replace(pokemon, details, healthStatus) =>
      this.updateActivePokemon(pokemon)(p => p.copy(teamPokemon = TeamPokemon(details, healthStatus)))
    case BattleMajorActionMessage.Swap(pokemon, slot) =>
      this.changePosition(pokemon, slot)
    case BattleMajorActionMessage.Faint(pokemon) =>
      this.updateActivePokemon(pokemon): p =>
        p.copy(teamPokemon = p.teamPokemon.copy(condition = p.teamPokemon.condition.faint))

    //Attack
    case BattleAttackMessage.Waiting(pokemon, _) =>
      this.updateActivePokemon(pokemon)(_.withNextMoveStatus(VolatileStatus.Waiting))
    case BattleAttackMessage.Prepare(pokemon, move, _) =>
      this.updateActivePokemon(pokemon)(_.withNextMoveStatus(VolatileStatus.fromMove(move)))
    case BattleAttackMessage.MustRecharge(pokemon) =>
      this.updateActivePokemon(pokemon)(_.withNextMoveStatus(VolatileStatus.MustRecharge))

    //Status
    case BattleStatusMessage.Damage(pokemon, condition) =>
      this.updateActivePokemon(pokemon)(p => p.copy(teamPokemon = p.teamPokemon.copy(condition = condition)))
    case BattleStatusMessage.Heal(pokemon, condition) =>
      this.updateActivePokemon(pokemon)(p => p.copy(teamPokemon = p.teamPokemon.copy(condition = condition)))
    case BattleStatusMessage.SetHealth(pokemon, health) =>
      this.updateActivePokemon(pokemon)(p => p.copy(teamPokemon = p.teamPokemon.withHealth(health)))
    case BattleStatusMessage.SetStatus(pokemon, status) =>
      this.updateActivePokemon(pokemon)(p => p.copy(teamPokemon = p.teamPokemon.withStatus(status)))
    case BattleStatusMessage.CureStatus(pokemon, _) =>
      this.updateActivePokemon(pokemon)(p => p.copy(teamPokemon = p.teamPokemon.cured))
    case BattleStatusMessage.Boost(pokemon, stat, amount) =>
      this.updateActivePokemon(pokemon)(_.boosted(stat, amount))
    case BattleStatusMessage.Unboost(pokemon, stat, amount) =>
      this.updateActivePokemon(pokemon)(_.boosted(stat, -amount))
    case BattleStatusMessage.SetBoost(pokemon, stat, amount) =>
      this.updateActivePokemon(pokemon)(_.withBoost(stat, amount))
    case BattleStatusMessage.InvertBoost(pokemon) =>
      this.updateActivePokemon(pokemon)(p => p.copy(boosts = p.boosts.map((k, v) => (k, -v))))
    case BattleStatusMessage.ClearBoost(pokemon) =>
      this.updateActivePokemon(pokemon)(_.boostsCleared)
    case BattleStatusMessage.ClearAllBoost =>
      this.updateAllActivePokemon(_.boostsCleared)
    case BattleStatusMessage.ClearPositiveBoost(target, _, _) =>
      this.updateActivePokemon(target)(p => p.copy(boosts = p.boosts.filter((k, v) => v > StatBoost(0))))
    case BattleStatusMessage.ClearNegativeBoost(target, _) =>
      this.updateActivePokemon(target)(p => p.copy(boosts = p.boosts.filter((k, v) => v < StatBoost(0))))
    case BattleStatusMessage.CopyBoost(pokemon, target) =>
      this.updateActivePokemon(pokemon): pkmn =>
        getActivePokemon(target) match
          case Some(targetPkmn) => pkmn.copy(boosts = targetPkmn.boosts)
          case None => pkmn
    case BattleStatusMessage.VolatileStatusStart(pokemon, status) =>
      this.updateActivePokemon(pokemon)(_.withVolatileStatus(status))
    case BattleStatusMessage.VolatileStatusEnd(pokemon, status) =>
      this.updateActivePokemon(pokemon)(_.removedVolatileStatus(status))
    case BattleStatusMessage.SingleMove(pokemon, move) =>
      this.updateActivePokemon(pokemon)(_.withNextMoveStatus(VolatileStatus.fromMove(move)))
    case BattleStatusMessage.SingleTurn(pokemon, move) =>
      this.updateActivePokemon(pokemon)(_.withNextTurnStatus(VolatileStatus.fromMove(move)))

    //Minor action
    case BattleMinorActionMessage.Weather(weather) => this.copy(weather = weather)
    case BattleMinorActionMessage.FieldStart(fieldEffect) =>
      this.copy(field = field.updated(fieldEffect, currentTurn.getOrElse(TurnNumber(1))))
    case BattleMinorActionMessage.FieldEnd(fieldEffect) =>
      this.copy(field = field.removed(fieldEffect))
    case BattleMinorActionMessage.SideStart(side, field) =>
      this.updateSide(side)(_.withEffect(field))
    case BattleMinorActionMessage.SideEnd(side, field) =>
      this.updateSide(side)(_.removedEffect(field))
    case BattleMinorActionMessage.SwapSideConditions => this
    case BattleMinorActionMessage.Item(pokemon, item, effect) =>
      this.updateActivePokemon(pokemon)(p => p.copy(teamPokemon = p.teamPokemon.revealedItem(item, effect)))
    case BattleMinorActionMessage.EndItem(pokemon, item, effect) =>
      this.updateActivePokemon(pokemon)(p => p.copy(teamPokemon = p.teamPokemon.destroyedItem(item, effect)))
    case BattleMinorActionMessage.Ability(pokemon, ability, None) =>
      this.updateActivePokemon(pokemon)(p => p.copy(teamPokemon = p.teamPokemon.revealedAbility(ability)))
    case BattleMinorActionMessage.Ability(pokemon, ability, Some(cause)) =>
      this.updateActivePokemon(pokemon)(_.withModifiedAbility(ability, cause))
    case BattleMinorActionMessage.EndAbility(pokemon) =>
      this.updateActivePokemon(pokemon)(_.disabledAbility)
    case BattleMinorActionMessage.Transform(pokemon, target, _) =>
      this.updateActivePokemon(pokemon): pkmn =>
        getActivePokemon(target) match
          case Some(targetPkmn) => pkmn.transformedInto(targetPkmn)
          case None => pkmn
    case BattleMinorActionMessage.UltraBurst(pokemon, species, _) =>
      this.updateActivePokemon(pokemon)(_.copy(transformedSpecies = Some(species)))
    case BattleMinorActionMessage.Center => ???


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
    weather = None,
    field = Map.empty,
    sides = Map.empty
  )