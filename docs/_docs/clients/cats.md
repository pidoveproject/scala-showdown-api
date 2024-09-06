---
title: Cats Effect Client
---

# Cats Effect client

A CE-based [[ShowdownClient|io.github.pidoveproject.showdown.client.ShowdownClient]]. Each method returns an `IO` program and is not effectful in itself.

## Connecting to the server

You can create a [[ZIOShowdownClient|io.github.pidoveproject.showdown.client.cats.CatsShowdownClient]] from a [[CatsShowdownClient|io.github.pidoveproject.showdown.client.cats.CatsShowdownClient]] then use [[CatsShowdownClient#openConnection|io.github.pidoveproject.showdown.client.cats.CatsShowdownClient.openConnection]] to connect to the Pokemon Showdown server.

Note that `openConnection` only returns a program. It does not perform any effect and need to be run by the ZIO runtime (e.g using a [[IOApp|cats.effect.IOApp]]).

## Receiving messages

Server-sent messages can be treated as a [[Stream|fs2.Stream]] using [[CatsShowdownConnection#serverMessages|io.github.pidoveproject.showdown.client.cats.CatsShowdownConnection.serverMessages]].

```scala
val connection: CatsShowdownConnection = ???
val consumeUntilStop: IO[Unit] = connection
  .serverMessages
  .evalMap:
    case Right(GlobalMessage.PrivateMessage(User(sender, _), _, content)) if content.value.equalsIgnoreCase("ping") =>
      for
        _ <- Console.printLine(s"< $sender: Ping!")
        _ <- connection.sendPrivateMessage(sender, ChatContent("Pong!"))
        _ <- Console.printLine(s"> $sender: Pong!")
      yield
        true

    case Right(GlobalMessage.PrivateMessage(User(sender, _), _, content)) if content.value.equalsIgnoreCase("stop") =>
      for
        _ <- Console.printLine(s"< $sender: Received stop command")
        _ <- connection.sendPrivateMessage(sender, ChatContent("Goodbye!"))
      yield
        false

    case Left(error) =>
      for
        _ <- Console.printLine(s"An error occurred: $sender")
      yield
        true
  .takeWhile(identity)
  .drain
```