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
    activePokemon: Map[PokemonPosition, ActivePokemon],
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

  def getActivePokemon(position: PokemonPosition): Option[ActivePokemon] =
    activePokemon.get(position)

  def withActivePokemon(position: PokemonPosition, pokemon: ActivePokemon): Battle =
    this.copy(activePokemon = activePokemon.updated(position, pokemon))

  def updateActivePokemon(position: PokemonPosition)(f: ActivePokemon => ActivePokemon): Battle =
    this.copy(activePokemon = activePokemon.updatedWith(position)(_.map(f)))

  def updateAllActivePokemon(f: ActivePokemon => ActivePokemon): Battle =
    this.copy(activePokemon = activePokemon.map((k, v) => (k, f(v))))

  def getTeamPokemonAt(position: PokemonPosition): Option[TeamPokemon] =
    getActivePokemon(position).flatMap(p => getTeamPokemon(position.player, p.teamSlot))

  def getTeamPokemon(player: PlayerNumber, slot: TeamSlot): Option[TeamPokemon] =
    for
      owner <- players.get(player)
      team <- owner.team
      teamPokemon <- team.getPokemon(slot)
    yield
      teamPokemon

  def withTeamPokemonAt(position: PokemonPosition, teamPokemon: TeamPokemon): Battle =
    val result =
      for
        activePokemon <- getActivePokemon(position)
        slot = activePokemon.teamSlot
        owner <- players.get(position.player)
        team <- owner.team
      yield
        val updatedPlayer = owner.copy(team = Some(team.withPokemon(slot, teamPokemon)))
        this.copy(players = players.updated(position.player, updatedPlayer))

    result.getOrElse(this)

  def updateTeamPokemonAt(position: PokemonPosition)(f: TeamPokemon => TeamPokemon): Battle =
    val result =
      for
        activePokemon <- getActivePokemon(position)
        slot = activePokemon.teamSlot
        owner <- players.get(position.player)
        team <- owner.team
        teamPokemon <- team.getPokemon(activePokemon.teamSlot)
      yield
        val updatedPlayer = owner.copy(team = Some(team.withPokemon(slot, f(teamPokemon))))
        this.copy(players = players.updated(position.player, updatedPlayer))

    result.getOrElse(this)

  def getTeamSlot(playerNumber: PlayerNumber, details: PokemonDetails): Option[TeamSlot] =
    for
      player <- players.get(playerNumber)
      team <- player.team
      slot <- team.getSlotByDetails(details)
    yield
      slot

  def declareTeamPokemon(playerNumber: PlayerNumber, teamPokemon: TeamPokemon): (Option[TeamSlot], Battle) =
    val result =
      for
        player <- players.get(playerNumber)
        team <- player.team
        availableSlot <- team.firstAvailableSlot
      yield
        val updatedPlayer = player.copy(team = Some(team.withPokemon(availableSlot, teamPokemon)))
        (Some(availableSlot), this.copy(players = players.updated(playerNumber, updatedPlayer)))

    result.getOrElse((None, this))

  def transformPokemon(pokemonPosition: PokemonPosition, targetPosition: PokemonPosition): Battle =
    val transformedPokemon =
      for
        activePokemon <- getActivePokemon(pokemonPosition)
        teamPokemon <- getTeamPokemonAt(pokemonPosition)
        activeTarget <- getActivePokemon(targetPosition)
        teamTarget <- getTeamPokemonAt(targetPosition)
      yield
        val targetAbility = activeTarget.modifiedAbility.orElse(teamTarget.ability.map(RevealedAbility.Base.apply))
        val targetSpecies = activeTarget.transformedSpecies.getOrElse(teamTarget.details.species)

        activePokemon.copy(modifiedAbility = targetAbility, transformedSpecies = Some(targetSpecies))

    transformedPokemon.fold(this)(pokemon => this.withActivePokemon(pokemonPosition, pokemon))

  def changeActiveSlot(position: PokemonPosition, slot: PokemonSlot): Battle =
    getActivePokemon(position) match
      case Some(pokemon) =>
        val newPos = position.copy(slot = slot)
        this.copy(activePokemon = activePokemon.removed(position).updated(newPos, pokemon))
      case None => this

  def updateSide(side: PlayerPosition)(f: SideCondition => SideCondition): Battle =
    this.copy(sides = sides.updatedWith(side)(_.map(f)))

  def update(message: BattleMessage): Battle = message match

    //Initialization
    case BattleInitializationMessage.Player(number, name, avatar, rating) =>
      players.get(number) match
        case Some(player) => this.copy(players = players.updated(number, player.copy(name = name, avatar = avatar, rating = rating)))
        case None =>
          this.copy(players = players.updated(number, Player(number, name, avatar, rating)))
    case BattleInitializationMessage.TeamSize(player, size) =>
      this.updatePlayer(player, _.copy(team = Some(PlayerTeam(size))))
    case BattleInitializationMessage.GameType(battleType) => this.copy(battleType = Some(battleType))
    case BattleInitializationMessage.Gen(generation) => this.copy(generation = Some(generation))
    case BattleInitializationMessage.Tier(format) => this.copy(format = Some(format))
    case BattleInitializationMessage.Rule(rule) => this.copy(rules = rules + rule)
    case BattleInitializationMessage.StartPreview() => this.copy(state = BattleState.Preview)
    case BattleInitializationMessage.Start() => this.copy(state = BattleState.Playing)
    case BattleInitializationMessage.DeclarePokemon(player, details, item) =>
      this.declareTeamPokemon(player, TeamPokemon(details, item = item.fold(HeldItem.Unknown)(HeldItem.Revealed(_, None))))._2

    //Progress
    case BattleProgressMessage.TimerMessage(_) => this.copy(timerEnabled = true)
    case BattleProgressMessage.TimerDisabled(_) => this.copy(timerEnabled = false)
    case BattleProgressMessage.Turn(turn) => this.copy(currentTurn = Some(turn))
    case BattleProgressMessage.Win(user) => this.copy(result = Some(BattleResult.Win(user)))
    case BattleProgressMessage.Tie() => this.copy(result = Some(BattleResult.Tie))

    //Major action
    case BattleMajorActionMessage.Switch(pokemon, details, healthStatus) =>
      val playerNumber = pokemon.position.player
      getTeamSlot(playerNumber, details) match
        case Some(slot) => this.withActivePokemon(pokemon.position, ActivePokemon(slot))
        case None =>
          declareTeamPokemon(playerNumber, TeamPokemon(details, healthStatus)) match
            case (Some(slot), battle) => battle.withActivePokemon(pokemon.position, ActivePokemon(slot))
            case (None, battle) => battle

    case BattleMajorActionMessage.DetailsChange(pokemon, details, healthStatus) =>
      this.updateTeamPokemonAt(pokemon.position): p =>
        p.copy(details = p.details.merge(details), condition = healthStatus.getOrElse(p.condition))
    case BattleMajorActionMessage.Replace(pokemon, details, healthStatus) =>
      this.withTeamPokemonAt(pokemon.position, TeamPokemon(details, healthStatus))
    case BattleMajorActionMessage.Swap(pokemon, slot) =>
      this.changeActiveSlot(pokemon.position, slot)
    case BattleMajorActionMessage.Faint(pokemon) =>
      this.updateTeamPokemonAt(pokemon.position)(p => p.copy(condition = p.condition.faint))

    //Attack
    case BattleAttackMessage.Waiting(pokemon, _) =>
      this.updateActivePokemon(pokemon.position)(_.withNextMoveStatus(VolatileStatus.Waiting))
    case BattleAttackMessage.Prepare(pokemon, move, _) =>
      this.updateActivePokemon(pokemon.position)(_.withNextMoveStatus(VolatileStatus.fromMove(move)))
    case BattleAttackMessage.MustRecharge(pokemon) =>
      this.updateActivePokemon(pokemon.position)(_.withNextMoveStatus(VolatileStatus.MustRecharge))

    //Status
    case BattleStatusMessage.Damage(pokemon, condition) =>
      this.updateTeamPokemonAt(pokemon.position)(_.copy(condition = condition))
    case BattleStatusMessage.Heal(pokemon, condition) =>
      this.updateTeamPokemonAt(pokemon.position)(_.copy(condition = condition))
    case BattleStatusMessage.SetHealth(pokemon, health) =>
      this.updateTeamPokemonAt(pokemon.position)(_.withHealth(health))
    case BattleStatusMessage.SetStatus(pokemon, status) =>
      this.updateTeamPokemonAt(pokemon.position)(_.withStatus(status))
    case BattleStatusMessage.CureStatus(pokemon, _) =>
      this.updateTeamPokemonAt(pokemon.position)(_.cured)
    case BattleStatusMessage.Boost(pokemon, stat, amount) =>
      this.updateActivePokemon(pokemon.position)(_.boosted(stat, amount))
    case BattleStatusMessage.Unboost(pokemon, stat, amount) =>
      this.updateActivePokemon(pokemon.position)(_.boosted(stat, -amount))
    case BattleStatusMessage.SetBoost(pokemon, stat, amount) =>
      this.updateActivePokemon(pokemon.position)(_.withBoost(stat, amount))
    case BattleStatusMessage.InvertBoost(pokemon) =>
      this.updateActivePokemon(pokemon.position)(p => p.copy(boosts = p.boosts.map((k, v) => (k, -v))))
    case BattleStatusMessage.ClearBoost(pokemon) =>
      this.updateActivePokemon(pokemon.position)(_.boostsCleared)
    case BattleStatusMessage.ClearAllBoost =>
      this.updateAllActivePokemon(_.boostsCleared)
    case BattleStatusMessage.ClearPositiveBoost(target, _, _) =>
      this.updateActivePokemon(target.position)(p => p.copy(boosts = p.boosts.filter((k, v) => v > StatBoost(0))))
    case BattleStatusMessage.ClearNegativeBoost(target, _) =>
      this.updateActivePokemon(target.position)(p => p.copy(boosts = p.boosts.filter((k, v) => v < StatBoost(0))))
    case BattleStatusMessage.CopyBoost(pokemon, target) =>
      this.updateActivePokemon(pokemon.position): pkmn =>
        getActivePokemon(target.position) match
          case Some(targetPkmn) => pkmn.copy(boosts = targetPkmn.boosts)
          case None => pkmn
    case BattleStatusMessage.VolatileStatusStart(pokemon, status) =>
      this.updateActivePokemon(pokemon.position)(_.withVolatileStatus(status))
    case BattleStatusMessage.VolatileStatusEnd(pokemon, status) =>
      this.updateActivePokemon(pokemon.position)(_.removedVolatileStatus(status))
    case BattleStatusMessage.SingleMove(pokemon, move) =>
      this.updateActivePokemon(pokemon.position)(_.withNextMoveStatus(VolatileStatus.fromMove(move)))
    case BattleStatusMessage.SingleTurn(pokemon, move) =>
      this.updateActivePokemon(pokemon.position)(_.withNextTurnStatus(VolatileStatus.fromMove(move)))

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
      this.updateTeamPokemonAt(pokemon.position)(_.revealedItem(item, effect))
    case BattleMinorActionMessage.EndItem(pokemon, item, effect) =>
      this.updateTeamPokemonAt(pokemon.position)(_.destroyedItem(item, effect))
    case BattleMinorActionMessage.Ability(pokemon, ability, None) =>
      this.updateTeamPokemonAt(pokemon.position)(_.revealedAbility(ability))
    case BattleMinorActionMessage.Ability(pokemon, ability, Some(cause)) =>
      this.updateActivePokemon(pokemon.position)(_.withModifiedAbility(ability, cause))
    case BattleMinorActionMessage.EndAbility(pokemon) =>
      this.updateActivePokemon(pokemon.position)(_.disabledAbility)
    case BattleMinorActionMessage.Transform(pokemon, target, _) =>
      transformPokemon(pokemon.position, target.position)
    case BattleMinorActionMessage.UltraBurst(pokemon, species, _) =>
      this.updateActivePokemon(pokemon.position)(_.copy(transformedSpecies = Some(species)))
    case BattleMinorActionMessage.Center =>
      val centeredPokemon = activePokemon.map((k, v) => (k.copy(slot = PokemonSlot(0)), v))
      this.copy(activePokemon = centeredPokemon)


    case _ => this

object Battle:

  val empty: Battle = Battle(
    state = BattleState.Initialization,
    activePokemon = Map.empty,
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