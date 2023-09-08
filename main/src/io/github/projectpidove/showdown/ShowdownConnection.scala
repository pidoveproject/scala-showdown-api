package io.github.projectpidove.showdown

import io.github.projectpidove.showdown.protocol.LoginResponse
import io.github.projectpidove.showdown.protocol.client.{ClientMessage, GlobalCommand}
import io.github.projectpidove.showdown.protocol.server.ServerMessage
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
   * @param message the message to send
   */
  def sendMessage(message: ClientMessage): Cmd[Unit]

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

  def currentState: Cmd[ShowdownData]