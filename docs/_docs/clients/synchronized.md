---
title: Synchronized client
---

# Synchronized client

A side-effectful, synchronized implementation of [[ShowdownClient|io.github.pidoveproject.showdown.client.ShowdownClient]].

## Connecting to the server

You can use [[SyncShowdownClient.openConnection|io.github.pidoveproject.showdown.client.sync.SyncShowdownClient.openConnection]] to connect to the Pokemon Showdown server. This function returns a callback with a [[SyncShowdownConnection|io.github.pidoveproject.showdown.client.sync.SyncShowdownConnection]] you can consume to interact with the server.

```scala
SyncShowdownClient.openConnection(): connection =>
  ???
```

The connection is automatically closed once the callback finishes.

## Receiving messages

You can use [[SyncShowdownConnection#serverMessages|io.github.pidoveproject.showdown.client.sync.SyncShowdownConnection.serverMessages]] to process messages sent by the server. A boolean result is used to continue to process messages or not.

```scala
connection.serverMessages:
  case Right(GlobalMessage.PrivateMessage(User(sender, _), _, content)) if content.value.equalsIgnoreCase("ping") =>
    println(s"< $sender: Ping!")
    connection.sendPrivateMessage(sender, ChatContent("Pong !"))
    println(s"> $sender: Pong!")
    true //Continue

  case Right(GlobalMessage.PrivateMessage(User(sender, _), _, content)) if content.value.equalsIgnoreCase("stop") =>
    println(s"< $sender: Received stop command")
    connection.sendPrivateMessage(sender, ChatContent("Goodbye!"))
    false //End
```