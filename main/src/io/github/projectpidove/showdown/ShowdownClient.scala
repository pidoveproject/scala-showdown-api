package io.github.projectpidove.showdown

import io.github.projectpidove.showdown.protocol.server.ServerMessage

trait ShowdownClient[Frame, Cmd[_]]:

  def openConnection(handler: ShowdownConnection[Frame, Cmd] => Cmd[Unit]): Cmd[Unit]