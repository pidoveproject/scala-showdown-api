package io.github.projectpidove.showdown.client

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.projectpidove.showdown.*
import io.github.projectpidove.showdown.battle.*
import io.github.projectpidove.showdown.protocol.ProtocolError
import io.github.projectpidove.showdown.protocol.client.GlobalCommand
import io.github.projectpidove.showdown.protocol.server.choice.{ActiveChoice, ChoiceRequest, MoveChoice, PokemonChoice, TeamChoice}
import io.github.projectpidove.showdown.protocol.server.*
import io.github.projectpidove.showdown.room.RoomId
import io.github.projectpidove.showdown.user.Username
import zio.*
import zio.http.*

object Main extends ZIOAppDefault:

  /**
   * Login to an account.
   */
  private val loginProgram: ConnectionTask[Unit] =
    for
      _ <- Console.printLine("Logging in...").toProtocolZIO
      username <- Console.readLine("Username: ").toProtocolZIO.flatMap(Username.applyZIO(_)).retryN(3)
      password <- Console.readLine("Password: ").toProtocolZIO
      _ <- ZIOShowdownConnection.login(username, password)
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
        clientState <- ZIOShowdownConnection.currentState
        _ <- ZIO.unless(clientState.joinedRooms.contains(roomId))(ZIOShowdownConnection.sendMessage(GlobalCommand.Join(roomId)))
        _ <- Console.printLine(s"Using room $room").toProtocolZIO
      yield
        app.copy(currentRoom = Some(roomId))

    case "use" =>
      ZIO.succeed(app.copy(currentRoom = None))

    case "show teams" =>
      for
        battle <- app.currentBattle.someOrFail(ProtocolError.Miscellaneous("No battle in current room"))
        teams =
          battle
            .players
            .map:
              case (num, Player(_, _, _, _, Some(team))) => showTeam(num, team)
              case (num, _) => s"=== Player $num ===\nNo team"

        _ <- Console.printLine(teams.mkString("\n")).toProtocolZIO
      yield
        app

    case "show active" =>
      for
        battle <- app.currentBattle.someOrFail(ProtocolError.Miscellaneous("No battle in current room"))
        _ <- Console.printLine(showAllActive).toProtocolZIO
      yield
        app

    case "login" =>
      for
        state <- ZIOShowdownConnection.currentState
        _ <-
          if state.challStr.isDefined then loginProgram.catchAll(err => Console.printLine(err.getMessage).toProtocolZIO)
          else Console.printLine("Cannot login at the moment: handshaked is not finished.").toProtocolZIO
      yield
        app

    case "stop" => Console.printLine("Stopping client...").toProtocolZIO *> ZIO.succeed(app)

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
  private def subscribeProgram(connection: ShowdownConnection[WebSocketFrame, ProtocolTask], appRef: Ref[ClientApp])(message: ServerMessage): ProtocolTask[Unit] =
    for
      app <- appRef.get
      _ <- ZIO.when(app.debugging)(Console.printLine(s"< $message").toProtocolZIO)
      _ <- message match
        case GlobalMessage.ChallStr(challstr) =>
          Console.printLine("Login available").toProtocolZIO

        case RoomBoundMessage(_, BattleProgressMessage.Request(choice)) =>
          Console.printLine(showChoiceRequest(choice)).toProtocolZIO

        case _ => ZIO.unit
    yield
      ()

  /**
   * Launch the client's main logic using the given connection.
   * 
   * @param connection the connection to use
   */
  private def connectionProgram(connection: ShowdownConnection[WebSocketFrame, ProtocolTask]): ProtocolTask[Unit] =
    for
      appRef <- Ref.make(ClientApp())
      _ <- commandProgram(appRef).provide(ZLayer.succeed(connection)) raceFirst connection.subscribe(subscribeProgram(connection, appRef))
    yield
      ()

  override def run =
    ZIOShowdownClient
      .openConnection(connectionProgram)
      .provide(
        Client.default,
        ZIOShowdownClient.layer()
      )