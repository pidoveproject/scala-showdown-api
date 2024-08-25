package io.github.pidoveproject.showdown.example

import io.github.pidoveproject.showdown.user.Username
import scala.util.CommandLineParser
import io.github.pidoveproject.showdown.client.sync.SyncShowdownClient
import io.github.pidoveproject.showdown.ChallStr
import io.github.pidoveproject.showdown.protocol.server.GlobalMessage
import io.github.pidoveproject.showdown.user.User
import io.github.pidoveproject.showdown.room.ChatContent
import io.github.iltotore.iron.*

given CommandLineParser.FromString[Username] = Username.applyUnsafe(_)

@main
def mainProgram(name: Username, password: String) =
  println("Connecting to server...")
  SyncShowdownClient.connect(): (client, connection) =>
    println("Connected to the server")
    connection.serverMessages:
      case Right(GlobalMessage.ChallStr(challstr)) =>
        println("Logging in...")
        val response = client.login(challstr)(name, password)
        connection.confirmLogin(name, response.assertion)
        println("Logged in!")
        true

      case Right(GlobalMessage.PrivateMessage(User(sender, _), _, content)) if content.value.equalsIgnoreCase("ping") =>
        println(s"< $sender: Ping!")
        connection.sendPrivateMessage(sender, ChatContent("Pong !"))
        println(s"> $sender: Pong!")
        true

      case Right(GlobalMessage.PrivateMessage(User(sender, _), _, content)) if content.value.equalsIgnoreCase("stop") =>
        println(s"< $sender: Received stop command")
        connection.sendPrivateMessage(sender, ChatContent("Goodbye!"))
        false

    println("Disconnecting...")