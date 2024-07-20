package io.github.pidoveproject.showdown

import io.github.pidoveproject.showdown.protocol.{LoginResponse, ProtocolError}
import io.github.pidoveproject.showdown.protocol.client.{ClientMessage, GlobalCommand, OptionCommand}
import io.github.pidoveproject.showdown.protocol.server.ServerMessage
import io.github.pidoveproject.showdown.room.{ChatContent, RoomId}
import io.github.pidoveproject.showdown.user.Username

/**
 * A connection established with a Pokemon Showdown server.
 *
 * @tparam Frame the type of web socket frame
 * @tparam Task the type of a task
 * @tparam Stream the type of a stream
 */
trait ShowdownConnection[Frame, Task[_], Stream[_, _]]:

  /**
   * Send a socket frame to the server.
   *
   * @param message the socket message to send
   */
  def sendRawMessage(message: Frame): Task[Unit]

  /**
   * Send client-bound message to the server.
   *
   * @param room the room to send the message to
   * @param message the message to send
   */
  def sendMessage(room: RoomId, message: ClientMessage): Task[Unit]

  /**
   * Send client-bound message to the server.
   *
   * @param message the message to send
   */
  def sendMessage(message: ClientMessage): Task[Unit]

  /**
   * Send a private message to another player.
   *
   * @param recipient the recipient of the message
   * @param message the message to send
   */
  def sendPrivateMessage(recipient: Username, message: ChatContent): Task[Unit] =
    sendMessage(GlobalCommand.Msg(recipient, message))

  /**
   * Disconnect the user
   */
  def logout(): Task[Unit] =
    sendMessage(GlobalCommand.LogOut)

  /**
   * Accept a challenge
   */
  def acceptChallenge(): Task[Unit] =
    sendMessage(OptionCommand.UnblockChallenges)

  /**
   * Challenge another user
   *
   * @param user the user to challenge
   * @param format the format of the battle
   */
  def challengeUser(user: Username, format: FormatName): Task[Unit] =
    sendMessage(GlobalCommand.Challenge(user, format))

  /**
   * Search for a battle
   *
   * @param format the format to search for
   */
  def searchBattle(format: FormatName): Task[Unit] =
    sendMessage(GlobalCommand.Search(format))

  /**
   * Cancel the match search.
   */
  def cancelSearch(): Task[Unit] = sendMessage(GlobalCommand.CancelSearch)

  /**
   * Rename current user.
   *
   * @param name the new name to take
   */
  def rename(name: Username): Task[Unit] = sendMessage(OptionCommand.Nick(Some(name)))

  /**
   * Reset current user's name.
   */
  def resetName(): Task[Unit] = sendMessage(OptionCommand.Nick(None))

  /**
   * Join a room.
   *
   * @param room the id of the room to join
   */
  def joinRoom(room: RoomId): Task[Unit] =
    sendMessage(GlobalCommand.Join(room))

  /**
   * Leave a room
   *
   * @param room the id of the room to leave
   */
  def leaveRoom(room: RoomId): Task[Unit] =
    sendMessage(GlobalCommand.Leave(Some(room)))

  /**
   * Disconnect from the server.
   */
  def disconnect(): Task[Unit]

  /**
   * The stream of the received server messages
   */
  def serverMessages: Stream[ProtocolError, ServerMessage]

  /**
   * Login to a registered account.
   *
   * @param challStr the token used for authentication
   * @param name the name of the account
   * @param password the password of the account
   * @return the authentication response sent by the server
   */
  def login(challStr: ChallStr)(name: Username, password: String): Task[LoginResponse]

  /**
   * Login as guest.
   *
   * @param challStr the token used for authentication
   * @param name the name to take in game
   *
   * @return the guest's name if the authentication succeeds
   */
  def loginGuest(challStr: ChallStr)(name: Username): Task[String]