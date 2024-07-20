package io.github.pidoveproject.showdown

import io.github.pidoveproject.showdown.protocol.ProtocolError

/**
 * A client to communicate with a Pokemon Showdown server. This is the main entry point of the API.
 *
 * @tparam Frame the type of web socket frame
 * @tparam Task the type of a task
 * @return the authentication response sent by the server
 */
trait ShowdownClient[Frame, Task[_, _], Stream[_, _]]:

  /**
   * Open a connection to the pokemon showdown server.
   *
   * @param handler the program to execute while the connection is active. The connection ends when the handler finishes.
   */
  def openConnection(handler: ShowdownConnection[Frame, Task, Stream] => Task[ProtocolError, Unit]): Task[ProtocolError, Unit]