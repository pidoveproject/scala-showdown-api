package io.github.pidoveproject.showdown.client

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.pidoveproject.showdown.*
import io.github.pidoveproject.showdown.battle.*
import io.github.pidoveproject.showdown.protocol.ProtocolError
import io.github.pidoveproject.showdown.protocol.client.GlobalCommand
import io.github.pidoveproject.showdown.protocol.server.choice.{ActiveChoice, ChoiceRequest, MoveChoice, PokemonChoice, TeamChoice}
import io.github.pidoveproject.showdown.protocol.server.*
import io.github.pidoveproject.showdown.room.RoomId
import io.github.pidoveproject.showdown.user.*
import io.github.pidoveproject.showdown.client.zio.*
import _root_.zio.*
import _root_.zio.http.*
import _root_.zio.stream.*

object Main extends ZIOAppDefault:

  /**
   * Login to an account.
   */
  private def loginProgram(challStr: ChallStr): ConnectionTask[Unit] =
    for
      _ <- Console.printLine("Logging in...").toProtocolZIO
      username <- Console.readLine("Username: ").toProtocolZIO.flatMap(Username.applyZIO(_)).retryN(3)
      password <- Console.readLine("Password: ").toProtocolZIO
      response <- ZIOShowdownClient.login(challStr)(username, password)
      _ <- ZIOShowdownConnection.confirmLogin(response.currentUser.username, response.assertion)
    yield
      ()

  private def showTeams(app: ClientApp): ConnectionTask[Unit] =
    for
      battle <- ZIO.fromOption(app.currentBattle).mapError(_ => ProtocolError.Miscellaneous("No battle in current room"))
      teams =
        battle
          .players
          .map:
            case (num, Player(_, _, _, _, Some(team))) => showTeam(num, team)
            case (num, _) => s"=== Player $num ===\nNo team"

      _ <- Console.printLine(teams.mkString("\n")).toProtocolZIO
    yield
      ()

  /**
   * Perform the given command.
   * 
   * @param command the command to process
   * @param app the current state of the app
   * @return the new state of the client
   */
  private def processCommand(command: String, app: ClientApp): ConnectionTask[ClientApp] = command match
    case "debug" =>
      val updated = app.copy(debugging = !app.debugging)
      Console.printLine(s"Debug: ${updated.debugging}").toProtocolZIO
        *> ZIO.succeed(updated)

    case s"use $room" =>
      for
        roomId <- RoomId.applyZIO(room)
        _ <- ZIO.unless(app.currentState.joinedRooms.contains(roomId))(ZIOShowdownConnection.sendMessage(GlobalCommand.Join(roomId)))
        _ <- Console.printLine(s"Using room $room").toProtocolZIO
      yield
        app.copy(currentRoom = Some(roomId))

    case "use" =>
      ZIO.succeed(app.copy(currentRoom = None))

    case "show teams" =>
      showTeams(app) *> ZIO.succeed(app)

    case "show team" =>
      for
        battle <- ZIO.fromOption(app.currentBattle).mapError(_ => ProtocolError.Miscellaneous("No battle in current room"))
        team = battle.currentRequest match
          case Some(choice) => showFullTeamChoice(choice.team)
          case None => "No team choice"
        _ <- Console.printLine(team).toProtocolZIO
      yield
        app

    case "show active" =>
      for
        battle <- ZIO.fromOption(app.currentBattle).mapError(_ => ProtocolError.Miscellaneous("No battle in current room"))
        _ <- Console.printLine(showAllActive(battle)).toProtocolZIO
      yield
        app

    case "login" =>
      for
        _ <-
          app.currentState.challStr match
            case Some(challStr) => loginProgram(challStr).catchAll(err => Console.printLine(err.getMessage).toProtocolZIO)
            case None => Console.printLine("Cannot login at the moment: handshake is not finished.").toProtocolZIO
      yield
        app

    case "stop" => Console.printLine("Stopping client...").toProtocolZIO *> ZIO.succeed(app)

    case "choice" =>
      for
        choice <-
          app.currentBattle.flatMap(_.currentRequest) match
            case Some(choice) => Console.printLine(showBattleState(app.currentBattle.get, choice)).toProtocolZIO
            case None => Console.printLine("No choice request to display.").toProtocolZIO
      yield
        app

    case "" => ZIO.succeed(app)

    case message =>
      for
        _ <- ZIOShowdownConnection.sendRawMessage(WebSocketFrame.text(s"${app.currentRoom.fold("")(_.value)}|$message"))
      yield
        app

  /**
   * Receive a command as user's input and process it.
   * 
   * @param appRef the reference to the state of the client
   * @return the processed command
   */
  private def receiveCommand(appRef: Ref[ClientApp]): ConnectionTask[String] =
    for
      command <- Console.readLine("> ").toProtocolZIO
      app <- appRef.get
      updated <- processCommand(command, app)
      _ <- appRef.set(updated)
    yield
      command

  /**
   * The command line interface of the client.
   * 
   * @param appRef the reference to the state of the client
   */
  private def commandProgram(appRef: Ref[ClientApp]): ConnectionTask[Unit] =
    receiveCommand(appRef).repeatUntilEquals("stop").unit

  /**
   * Process the received message.
   * 
   * @param connection the current connection to Pokemon Showdown
   * @param appRef the reference to the state of the client
   * @param message the received message to process
   */
  private def subscribeProgram(appRef: Ref[ClientApp])(message: ServerMessage): ConnectionTask[Unit] =
    for
      app <- appRef.updateAndGet(a => a.copy(currentState = a.currentState.update(message)))
      _ <- ZIO.when(app.debugging)(Console.printLine(s"< $message").toProtocolZIO)
      _ <- message match
        case GlobalMessage.ChallStr(challstr) =>
          Console.printLine("Login available").toProtocolZIO

        case RoomBoundMessage(_, RoomMessage.Message(content)) => Console.printLine(content).toProtocolZIO

        case RoomBoundMessage(_, RoomMessage.Chat(User(name, _), content)) => Console.printLine(s"<$name> $content").toProtocolZIO

        case RoomBoundMessage(room, BattleInitializationMessage.StartPreview()) =>
          Console.printLine(s"===== Preview Start =====").toProtocolZIO
            *> showTeams(app)
            *> appRef.update(_.copy(currentRoom = Some(room)))

        case RoomBoundMessage(room, BattleInitializationMessage.Start()) =>
          Console.printLine(s"===== Battle Start =====").toProtocolZIO *> appRef.update(_.copy(currentRoom = Some(room)))

        case RoomBoundMessage(_, BattleProgressMessage.Turn(number)) => Console.printLine(s"===== Turn $number =====").toProtocolZIO

        case RoomBoundMessage(_, BattleProgressMessage.Win(user)) => Console.printLine(s"$user won the battle").toProtocolZIO

        case RoomBoundMessage(_, BattleMajorActionMessage.Switch(pokemon, details, condition, _)) =>
          Console.printLine(s"${details.species} (${condition.health.current}/${condition.health.max}) switched-in position ${pokemon.position}").toProtocolZIO

        case RoomBoundMessage(room, BattleMajorActionMessage.Move(attackerPos, move, _)) =>
          for
            app <- appRef.get
            battle <- ZIO.fromOption(app.currentState.getJoinedRoomOrEmpty(room).battle).orElseFail(ProtocolError.Miscellaneous("Received request from unjoined room"))
            attacker <- ZIO.fromOption(battle.getTeamMemberAt(attackerPos.position)).orElseFail(ProtocolError.Miscellaneous(s"Missing pokemon at $attackerPos"))
            _ <- Console.printLine(s"${attacker.details.species} used $move").toProtocolZIO
          yield
            ()

        case RoomBoundMessage(_, BattleAttackMessage.Fail(ActiveId(_, name), move)) =>
          Console.printLine(s"$name failed $move").toProtocolZIO

        case RoomBoundMessage(_, BattleAttackMessage.Miss(_, ActiveId(_, name))) =>
          Console.printLine(s"$name avoided the attack").toProtocolZIO

        case RoomBoundMessage(_, BattleAttackMessage.Block(ActiveId(_, name), effect, _)) =>
          Console.printLine(s"$name blocked the attack using $effect").toProtocolZIO

        case RoomBoundMessage(_, BattleAttackMessage.SuperEffective(ActiveId(_, name))) =>
          Console.printLine(s"It's super effective on $name").toProtocolZIO

        case RoomBoundMessage(_, BattleAttackMessage.Resisted(ActiveId(_, name))) =>
          Console.printLine(s"It's not very effective on $name").toProtocolZIO

        case RoomBoundMessage(_, BattleAttackMessage.Immune(ActiveId(_, name))) =>
          Console.printLine(s"It does not affect $name").toProtocolZIO

        case RoomBoundMessage(_, BattleAttackMessage.CriticalHit(ActiveId(_, name))) =>
          Console.printLine(s"A critical hit on $name!").toProtocolZIO

        case RoomBoundMessage(_, BattleStatusMessage.Damage(ActiveId(_, name), condition)) =>
          Console.printLine(s"$name was damaged (${showCondition(condition)})").toProtocolZIO

        case RoomBoundMessage(_, BattleStatusMessage.Heal(ActiveId(_, name), condition)) =>
          Console.printLine(s"$name was healed (${showCondition(condition)})").toProtocolZIO

        case RoomBoundMessage(_, BattleStatusMessage.SetStatus(ActiveId(_, name), status)) =>
          Console.printLine(s"$name now has status $status").toProtocolZIO

        case RoomBoundMessage(_, BattleStatusMessage.CureStatus(ActiveId(_, name), status)) =>
          Console.printLine(s"$name cured from $status").toProtocolZIO

        case RoomBoundMessage(_, BattleStatusMessage.Boost(ActiveId(_, name), stat, amount)) =>
          Console.printLine(s"$name's $stat raised by $amount").toProtocolZIO

        case RoomBoundMessage(_, BattleStatusMessage.Unboost(ActiveId(_, name), stat, amount)) =>
          Console.printLine(s"$name's $stat decreased by $amount").toProtocolZIO

        case _ => ZIO.unit
    yield
      ()

  /**
   * Launch the client's main logic using the given connection.
   * 
   * @param connection the connection to use
   */
  private def connectionProgram: ZIO[ZIOShowdownClient & ZIOShowdownConnection, Throwable, Unit] =
    def runStream(appRef: Ref[ClientApp]) =
      val subscribe = subscribeProgram(appRef)

      ZIOShowdownConnection
        .serverMessages
        .mapZIO:
          case Right(message) => subscribe(message)
          case Left(error) => Console.printLineError(s"Decoding error: $error").toProtocolZIO
        .runDrain

    for
      appRef <- Ref.make(ClientApp(ShowdownData.empty))
      _ <- commandProgram(appRef) raceFirst runStream(appRef)
    yield
      ()

  override def run: ZIO[Any, Throwable, Unit] =
    ZIO.scoped(
      ZIOShowdownClient
        .openConnection()
        .flatMap(connection => connectionProgram.provideSome[ZIOShowdownClient](ZLayer.succeed(connection)))
    ).provide(
        Client.default,
        ZIOShowdownClient.layer
      )