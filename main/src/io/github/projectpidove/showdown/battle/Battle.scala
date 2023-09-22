package io.github.projectpidove.showdown.battle

import io.github.projectpidove.showdown.protocol.server.choice.ChoiceRequest
import io.github.projectpidove.showdown.{FormatName, Generation}
import io.github.projectpidove.showdown.protocol.server.{BattleInitializationMessage, BattleMessage, BattleProgressMessage}
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
    currentRequest: Option[ChoiceRequest]
):

  def updatePlayer(number: PlayerNumber, f: Player => Player): Battle =
    players.get(number).map(f).fold(this)(player => this.copy(players = players.updated(number, player)))

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
    case BattleProgressMessage.Tie() => this.copy(result = Some(BattleResult.Tie()))


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
    currentRequest = None
  )