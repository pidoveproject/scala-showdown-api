package io.github.pidoveproject.showdown.client

import io.github.pidoveproject.showdown.protocol.URL
import io.github.iltotore.iron.autoRefine
import io.github.pidoveproject.showdown.user.Username
import io.github.pidoveproject.showdown.protocol.{Assertion, ProtocolError, LoginResponse}
import io.github.pidoveproject.showdown.ChallStr

/**
 * A client to communicate with a Pokemon Showdown server. This is the main entry point of the API.
 *
 * @tparam Frame the type of web socket frame
 * @tparam Task the type of a task
 * @return the authentication response sent by the server
 */
trait ShowdownClient[Frame, Task[_, _], Stream[_], Resource[+_]]:

  /**
   * Open a connection to the pokemon showdown server.
   */
  def openConnection(serverUrl: URL = URL("wss://sim3.psim.us/showdown/websocket")): Resource[ShowdownConnection[Frame, Task, Stream]]

  /**
   * Login to a registered account.
   *
   * @param challStr the token used for authentication
   * @param name the name of the account
   * @param password the password of the account
   * @return the authentication response sent by the server
   */
  def login(challStr: ChallStr)(name: Username, password: String): Task[ProtocolError, LoginResponse]

  /**
   * Login as guest.
   *
   * @param challStr the token used for authentication
   * @param name the name to take in game
   *
   * @return the guest's name if the authentication succeeds
   */
  def loginGuest(challStr: ChallStr)(name: Username): Task[ProtocolError, Assertion]
