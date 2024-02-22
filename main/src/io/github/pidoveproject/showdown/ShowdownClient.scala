package io.github.pidoveproject.showdown

/**
 * A client to communicate with a Pokemon Showdown server. This is the main entry point of the API.
 *
 * @tparam Frame the type of web socket frame
 * @tparam Cmd the type of a task/command
 */
trait ShowdownClient[Frame, Cmd[_]]:

  /**
   * Open a connection to the pokemon showdown server.
   *
   * @param handler the program to execute while the connection is active. The connection ends when the handler finishes.
   */
  def openConnection(handler: ShowdownConnection[Frame, Cmd] => Cmd[Unit]): Cmd[Unit]