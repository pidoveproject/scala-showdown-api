package io.github.projectpidove.showdown

import io.github.projectpidove.showdown.protocol.LoginResponse
import io.github.projectpidove.showdown.protocol.client.ClientMessage
import io.github.projectpidove.showdown.protocol.server.ServerMessage
import io.github.projectpidove.showdown.user.Username

trait ShowdownConnection[Frame, Cmd[_]]:

  def sendRawMessage(message: Frame): Cmd[Unit]

  def sendMessage(message: ClientMessage): Cmd[Unit]

  def disconnect(): Cmd[Unit]
  
  def subscribe(handler: ServerMessage => Cmd[Unit]): Cmd[Unit]

  def login(name: Username, password: String): Cmd[LoginResponse]

  def loginGuest(name: Username): Cmd[Unit]