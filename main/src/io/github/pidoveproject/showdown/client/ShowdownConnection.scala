package io.github.pidoveproject.showdown.client

import io.github.pidoveproject.showdown.protocol.client.{ClientMessage, GlobalCommand, OptionCommand}
import io.github.pidoveproject.showdown.protocol.server.ServerMessage
import io.github.pidoveproject.showdown.protocol.ProtocolError
import io.github.pidoveproject.showdown.room.{ChatContent, RoomId}
import io.github.pidoveproject.showdown.user.Username
import io.github.pidoveproject.showdown.protocol.Assertion
import io.github.pidoveproject.showdown.protocol.client.AuthCommand
import io.github.pidoveproject.showdown.FormatName

/**
 * A connection established with a Pokemon Showdown server.
 *
 * @tparam Frame the type of web socket frame
 * @tparam Task the type of a task
 * @tparam Stream the type of a stream
 */
trait ShowdownConnection[Frame, Task[_, _], Stream[_]]:

  /**
   * Send a socket frame to the server.
   *
   * @param message the socket message to send
   */
  def sendRawMessage(message: Frame): Task[ProtocolError, Unit]

  /**
   * Send client-bound message to the server.
   *
   * @param room the room to send the message to
   * @param message the message to send
   */
  def sendMessage(room: RoomId, message: ClientMessage): Task[ProtocolError, Unit]

  /**
   * Send client-bound message to the server.
   *
   * @param message the message to send
   */
  def sendMessage(message: ClientMessage): Task[ProtocolError, Unit]

  /**
   * Send a private message to another player.
   *
   * @param recipient the recipient of the message
   * @param message the message to send
   */
  def sendPrivateMessage(recipient: Username, message: ChatContent): Task[ProtocolError, Unit] =
    sendMessage(GlobalCommand.Msg(recipient, message))

  /**
   * Confirm login process by sending the assertion received from [[ShowdownClient#login]] or [[ShowdownClient#loginGuest]].
   *
   * @param name the username to take
   * @param assertion the assertion got from the authentication process
   */
  def confirmLogin(name: Username, assertion: Assertion): Task[ProtocolError, Unit] =
    sendMessage(AuthCommand.Trn(name, 0, assertion))

  /**
   * Disconnect the user
   */
  def logout(): Task[ProtocolError, Unit] =
    sendMessage(GlobalCommand.LogOut)

  /**
   * Accept a challenge
   */
  def acceptChallenge(): Task[ProtocolError, Unit] =
    sendMessage(OptionCommand.UnblockChallenges)

  /**
   * Challenge another user
   *
   * @param user the user to challenge
   * @param format the format of the battle
   */
  def challengeUser(user: Username, format: FormatName): Task[ProtocolError, Unit] =
    sendMessage(GlobalCommand.Challenge(user, format))

  /**
   * Search for a battle
   *
   * @param format the format to search for
   */
  def searchBattle(format: FormatName): Task[ProtocolError, Unit] =
    sendMessage(GlobalCommand.Search(format))

  /**
   * Cancel the match search.
   */
  def cancelSearch(): Task[ProtocolError, Unit] = sendMessage(GlobalCommand.CancelSearch)

  /**
   * Rename current user.
   *
   * @param name the new name to take
   */
  def rename(name: Username): Task[ProtocolError, Unit] = sendMessage(OptionCommand.Nick(Some(name)))

  /**
   * Reset current user's name.
   */
  def resetName(): Task[ProtocolError, Unit] = sendMessage(OptionCommand.Nick(None))

  /**
   * Join a room.
   *
   * @param room the id of the room to join
   */
  def joinRoom(room: RoomId): Task[ProtocolError, Unit] =
    sendMessage(GlobalCommand.Join(room))

  /**
   * Leave a room
   *
   * @param room the id of the room to leave
   */
  def leaveRoom(room: RoomId): Task[ProtocolError, Unit] =
    sendMessage(GlobalCommand.Leave(Some(room)))

  /**
   * Disconnect from the server.
   */
  def disconnect(): Task[ProtocolError, Unit]

  /**
   * The stream of the received server messages
   */
  def serverMessages: Stream[Either[ProtocolError, ServerMessage]]
