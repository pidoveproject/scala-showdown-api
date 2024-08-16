package io.github.pidoveproject.showdown

import io.github.pidoveproject.showdown.protocol.URL
import io.github.iltotore.iron.autoRefine

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
