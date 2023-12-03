package io.github.projectpidove.showdown

import io.github.projectpidove.showdown.protocol.LoginResponse
import io.github.projectpidove.showdown.protocol.client.{ClientMessage, GlobalCommand, OptionCommand}
import io.github.projectpidove.showdown.protocol.server.ServerMessage
import io.github.projectpidove.showdown.room.{ChatContent, RoomId}
import io.github.projectpidove.showdown.user.Username

/**
 * A connection established with a Pokemon Showdown server.
 *
 * @tparam Frame the type of web socket frame
 * @tparam Cmd the type of a task/command
 */
trait ShowdownConnection[Frame, Cmd[_]]:

  /**
   * Send a socket frame to the server.
   *
   * @param message the socket message to send
   */
  def sendRawMessage(message: Frame): Cmd[Unit]

  /**
   * Send client-bound message to the server.
   *
   * @param room the room to send the message to
   * @param message the message to send
   */
  def sendMessage(room: RoomId, message: ClientMessage): Cmd[Unit]

  /**
   * Send client-bound message to the server.
   *
   * @param message the message to send
   */
  def sendMessage(message: ClientMessage): Cmd[Unit]

  /**
   * Send a private message to another player.
   *
   * @param recipient the recipient of the message
   * @param message the message to send
   */
  def sendPrivateMessage(recipient: Username, message: ChatContent): Cmd[Unit] =
    sendMessage(GlobalCommand.Msg(recipient, message))

  /**
   * Disconnect the user
   */
  def logout(): Cmd[Unit] =
    sendMessage(GlobalCommand.LogOut)

  /**
   * Accept a challenge
   */
  def acceptChallenge(): Cmd[Unit] =
    sendMessage(OptionCommand.UnblockChallenges)

  /**
   * Challenge another user
   *
   * @param user the user to challenge
   * @param format the format of the battle
   */
  def challengeUser(user: Username, format: FormatName): Cmd[Unit] =
    sendMessage(GlobalCommand.Challenge(user, format))

  /**
   * Search for a battle
   *
   * @param format the format to search for
   */
  def searchBattle(format: FormatName): Cmd[Unit] =
    sendMessage(GlobalCommand.Search(format))

  /**
   * Cancel the match search.
   */
  def cancelSearch(): Cmd[Unit] = sendMessage(GlobalCommand.CancelSearch)

  /**
   * Rename current user.
   *
   * @param name the new name to take
   */
  def rename(name: Username): Cmd[Unit] = sendMessage(OptionCommand.Nick(Some(name)))

  /**
   * Reset current user's name.
   */
  def resetName(): Cmd[Unit] = sendMessage(OptionCommand.Nick(None))

  /**
   * Join a room.
   *
   * @param room the id of the room to join
   */
  def joinRoom(room: RoomId): Cmd[Unit] =
    sendMessage(GlobalCommand.Join(room))

  /**
   * Leave a room
   *
   * @param room the id of the room to leave
   */
  def leaveRoom(room: RoomId): Cmd[Unit] =
    sendMessage(GlobalCommand.Leave(Some(room)))

  /**
   * Disconnect from the server.
   */
  def disconnect(): Cmd[Unit]

  /**
   * Subscribe to the connection to receive messages.
   *
   * @param handler the message handler
   */
  def subscribe(handler: ServerMessage => Cmd[Unit]): Cmd[Unit]

  /**
   * Login to a registered account.
   *
   * @param name the name of the account
   * @param password the password of the account
   * @return the authentification response sent by the server
   */
  def login(name: Username, password: String): Cmd[LoginResponse]

  /**
   * Login as guest.
   *
   * @param name the name to take in game
   */
  def loginGuest(name: Username): Cmd[String]

  /**
   * The current state of the Showdown application.
   */
  def currentState: Cmd[ShowdownData]