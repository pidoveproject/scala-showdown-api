package io.github.projectpidove.showdown.battle

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.protocol.server.choice.ChoiceRequest
import io.github.projectpidove.showdown.{FormatName, Generation}
import io.github.projectpidove.showdown.protocol.server.{BattleAttackMessage, BattleInitializationMessage, BattleMajorActionMessage, BattleMessage, BattleMinorActionMessage, BattleProgressMessage, BattleStatusMessage}
import io.github.projectpidove.showdown.user.Username

import scala.math.Integral.Implicits.infixIntegralOps
import scala.math.Ordering.Implicits.infixOrderingOps

/**
 * The state of a battle. Represents all data about a battle.
 *
 * @param state the game state of this battle (initialization, playing...)
 * @param activePokemon the pokemon currently active on the battlefield
 * @param battleType the type of the battle (e.g singles)
 * @param players this battle's participants
 * @param generation this battle's generation
 * @param format this battle's format
 * @param rules this battle's rules
 * @param timerEnabled whether the time for each turn is limited or not
 * @param currentTurn the current turn of this battle
 * @param result the result of this battle if it ended
 * @param currentRequest the last pending choice request sent by the server
 * @param weather the weather currently active on the battlefield
 * @param field the currently active terrain effects
 * @param sides the currently active side-bound effects
 */
case class Battle(
                   state: BattleState,
                   activePokemon: Map[ActivePosition, ActivePokemon],
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
                   sides: Map[PlayerId, SideCondition]
):

  /**
   * Update a player represented by its number.
   *
   * @param number the numeric id of the player
   * @param f the function to apply to the player
   * @return a copy of this battle with the player updated
   */
  def updatePlayer(number: PlayerNumber, f: Player => Player): Battle =
    players.get(number).map(f).fold(this)(player => this.copy(players = players.updated(number, player)))

  /**
   * Get the pokemon active at the given position.
   *
   * @param position the position of the pokemon
   * @return the pokemon currently active at `position`
   */
  def getActivePokemon(position: ActivePosition): Option[ActivePokemon] =
    activePokemon.get(position)

  /**
   * Set the active pokemon at a given position.
   *
   * @param position the position to place the pokemon at
   * @param pokemon the pokemon to set at `position`
   * @return a copy of this battle with the given pokemon
   */
  def withActivePokemon(position: ActivePosition, pokemon: ActivePokemon): Battle =
    this.copy(activePokemon = activePokemon.updated(position, pokemon))

  /**
   * Update a pokemon represented by its position.
   *
   * @param position the position of the pokemon
   * @param f      the function to apply to the pokemon
   * @return a copy of this battle with the pokemon updated
   */
  def updateActivePokemon(position: ActivePosition)(f: ActivePokemon => ActivePokemon): Battle =
    this.copy(activePokemon = activePokemon.updatedWith(position)(_.map(f)))

  /**
   * Update all currently active pokemon.
   *
   * @param f the function to apply to the pokemon
   * @return a copy of this battle with the active pokemon updated
   */
  def updateAllActivePokemon(f: ActivePokemon => ActivePokemon): Battle =
    this.copy(activePokemon = activePokemon.map((k, v) => (k, f(v))))

  /**
   * Get the inactive data of the pokemon active at the given position.
   *
   * @param position the position of the pokemon
   * @return the [[TeamMember]] of the pokemon currently active at `position`
   */
  def getTeamMemberAt(position: ActivePosition): Option[TeamMember] =
    getActivePokemon(position).flatMap(p => getTeamMember(position.player, p.teamSlot))

  /**
   * Get the team member of a player.
   *
   * @param player the pokemon's owner
   * @param slot the slot of the pokemon in the player's team
   * @return the inactive pokemon at the given position
   */
  def getTeamMember(player: PlayerNumber, slot: TeamSlot): Option[TeamMember] =
    for
      owner <- players.get(player)
      team <- owner.team
      teamPokemon <- team.getPokemon(slot)
    yield
      teamPokemon

  /**
   * Set the inactive information of a pokemon currently active at a given position.
   *
   * @param position the current position of the pokemon
   * @param teamPokemon  the inactive information to set
   * @return a copy of this battle with the given pokemon
   */
  def withTeamMemberAt(position: ActivePosition, teamPokemon: TeamMember): Battle =
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

  /**
   * Update the inactive data of a currently active pokemon represented by its position.
   *
   * @param position the position of the pokemon
   * @param f        the function to apply to the pokemon
   * @return a copy of this battle with the pokemon updated
   */
  def updateTeamMemberAt(position: ActivePosition)(f: TeamMember => TeamMember): Battle =
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

  /**
   * Retrieve the slot of a team member using its details.
   *
   * @param playerNumber the owner of the member's team
   * @param details the details of the pokemon
   * @return the slot matching with the given details
   */
  def getTeamSlot(playerNumber: PlayerNumber, details: PokemonDetails): Option[TeamSlot] =
    for
      player <- players.get(playerNumber)
      team <- player.team
      slot <- team.getSlotByDetails(details)
    yield
      slot

  /**
   * Declare a new team member.
   *
   * @param playerNumber the pokemon's owner
   * @param teamPokemon the pokemon to add to the team
   * @return a copy of this battle with the given pokemon added
   */
  def declareTeamMember(playerNumber: PlayerNumber, teamPokemon: TeamMember): (Option[TeamSlot], Battle) =
    val result =
      for
        player <- players.get(playerNumber)
        team <- player.team
        availableSlot <- team.firstAvailableSlot
      yield
        val updatedPlayer = player.copy(team = Some(team.withPokemon(availableSlot, teamPokemon)))
        (Some(availableSlot), this.copy(players = players.updated(playerNumber, updatedPlayer)))

    result.getOrElse((None, this))

  /**
   * Transform a pokemon into another, like the ability.
   *
   * @param pokemonPosition the position of the pokemon to transform
   * @param targetPosition the position of the pokemon to copy
   * @return a copy of this battle with the pokemon transformed
   */
  def transformPokemon(pokemonPosition: ActivePosition, targetPosition: ActivePosition): Battle =
    val transformedPokemon =
      for
        activePokemon <- getActivePokemon(pokemonPosition)
        teamPokemon <- getTeamMemberAt(pokemonPosition)
        activeTarget <- getActivePokemon(targetPosition)
        teamTarget <- getTeamMemberAt(targetPosition)
      yield
        val targetAbility = activeTarget.modifiedAbility.orElse(teamTarget.ability.map(RevealedAbility.Base.apply))
        val targetSpecies = activeTarget.transformedSpecies.getOrElse(teamTarget.details.species)

        activePokemon.copy(modifiedAbility = targetAbility, transformedSpecies = Some(targetSpecies))

    transformedPokemon.fold(this)(pokemon => this.withActivePokemon(pokemonPosition, pokemon))

  /**
   * Change the local position of a pokemon.
   *
   * @param position the current position of the pokemon
   * @param slot the slot to move the pokemon to
   * @return a copy of this battle with the given pokemon moved
   */
  def changeActiveSlot(position: ActivePosition, slot: PokemonSlot): Battle =
    getActivePokemon(position) match
      case Some(pokemon) =>
        val newPos = position.copy(slot = slot)
        this.copy(activePokemon = activePokemon.removed(position).updated(newPos, pokemon))
      case None => this

  /**
   * Update the side condition of a player.
   *
   * @param side the side to update
   * @param f the function to apply to the side condition
   * @return a copy of this battle with the given side updated
   */
  def updateSide(side: PlayerId)(f: SideCondition => SideCondition): Battle =
    this.copy(sides = sides.updatedWith(side)(_.map(f)))

  /**
   * Update this data according to the passed server event/message.
   *
   * @param message the message sent by the server
   * @return a new [[Battle]] updated according to the given message
   */
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
      this.declareTeamMember(player, TeamMember(details, item = item.fold(HeldItem.Unknown)(HeldItem.Revealed(_, None))))._2

    //Progress
    case BattleProgressMessage.TimerMessage(_) => this.copy(timerEnabled = true)
    case BattleProgressMessage.TimerDisabled(_) => this.copy(timerEnabled = false)
    case BattleProgressMessage.Turn(turn) => this.copy(currentTurn = Some(turn))
    case BattleProgressMessage.Win(user) => this.copy(result = Some(BattleResult.Win(user)))
    case BattleProgressMessage.Tie() => this.copy(result = Some(BattleResult.Tie))

    //Major action
    case BattleMajorActionMessage.Switch(pokemon, details, condition, _) =>
      val playerNumber = pokemon.position.player
      getTeamSlot(playerNumber, details) match
        case Some(slot) => this.withActivePokemon(pokemon.position, ActivePokemon(slot))
        case None =>
          declareTeamMember(playerNumber, TeamMember(details, condition)) match
            case (Some(slot), battle) => battle.withActivePokemon(pokemon.position, ActivePokemon(slot))
            case (None, battle) => battle

    case BattleMajorActionMessage.DetailsChange(pokemon, details, condition) =>
      this.updateTeamMemberAt(pokemon.position): p =>
        p.copy(details = p.details.merge(details), condition = condition.getOrElse(p.condition))
    case BattleMajorActionMessage.Replace(pokemon, details, condition) =>
      this.withTeamMemberAt(pokemon.position, TeamMember(details, condition))
    case BattleMajorActionMessage.Swap(pokemon, slot) =>
      this.changeActiveSlot(pokemon.position, slot)
    case BattleMajorActionMessage.Faint(pokemon) =>
      this.updateTeamMemberAt(pokemon.position)(p => p.copy(condition = p.condition.faint))

    //Attack
    case BattleAttackMessage.Waiting(pokemon, _) =>
      this.updateActivePokemon(pokemon.position)(_.withNextMoveStatus(VolatileStatus.Waiting))
    case BattleAttackMessage.Prepare(pokemon, move, _) =>
      this.updateActivePokemon(pokemon.position)(_.withNextMoveStatus(VolatileStatus.fromMove(move)))
    case BattleAttackMessage.MustRecharge(pokemon) =>
      this.updateActivePokemon(pokemon.position)(_.withNextMoveStatus(VolatileStatus.MustRecharge))

    //Status
    case BattleStatusMessage.Damage(pokemon, condition) =>
      this.updateTeamMemberAt(pokemon.position)(_.copy(condition = condition))
    case BattleStatusMessage.Heal(pokemon, condition) =>
      this.updateTeamMemberAt(pokemon.position)(_.copy(condition = condition))
    case BattleStatusMessage.SetHealth(pokemon, health) =>
      this.updateTeamMemberAt(pokemon.position)(_.withHealth(health))
    case BattleStatusMessage.SetStatus(pokemon, status) =>
      this.updateTeamMemberAt(pokemon.position)(_.withStatus(status))
    case BattleStatusMessage.CureStatus(pokemon, _) =>
      this.updateTeamMemberAt(pokemon.position)(_.cured)
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
    case BattleMinorActionMessage.Weather(weather, _) => this.copy(weather = weather)
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
      this.updateTeamMemberAt(pokemon.position)(_.withRevealedItem(item, effect))
    case BattleMinorActionMessage.EndItem(pokemon, item, effect) =>
      this.updateTeamMemberAt(pokemon.position)(_.withDestroyedItem(item, effect))
    case BattleMinorActionMessage.Ability(pokemon, ability, None) =>
      this.updateTeamMemberAt(pokemon.position)(_.withRevealedAbility(ability))
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

  /**
   * An empty [[Battle]]. Typically the initial state of the data.
   */
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