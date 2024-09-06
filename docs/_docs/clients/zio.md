---
title: ZIO Client
---

# ZIO client

A ZIO-based [[ShowdownClient|io.github.pidoveproject.showdown.client.ShowdownClient]]. Each method returns a `ZIO` program and is not effectful in itself.

## Connecting to the server

You can create a [[ZIOShowdownClient|io.github.pidoveproject.showdown.client.zio.ZIOShowdownClient]] from a [[ZIO-HTTP Client|zio.http.Client]] then use [[ZIOShowdownClient#openConnection|io.github.pidoveproject.showdown.client.zio.ZIOShowdownClient.openConnection]] to connect to the Pokemon Showdown server.

```scala
val zHttpClient: Client = ???
val connectionProgram: ZIO[Scope, ProtocolError, ZIOShowdownConnection] = ZIOShowdownClient(zHttpClient).openConnection()
```

Note that `openConnection` only returns a program. It does not perform any effect and need to be run by the ZIO runtime (e.g using a [[ZIOApp|zio.ZIOApp]]).

## Receiving messages

Server-sent messages can be treated as a [[ZStream|zio.stream.ZStream]] using [[ZIOShowdownConnection#serverMessages|io.github.pidoveproject.showdown.client.zio.ZIOShowdownConnection.serverMessages]].

```scala
val connection: ZIOShowdownConnection = ???
val consumeUntilStop: Task[Unit] = connection
  .serverMessages
  .runForeachWhile:
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
```

## Using ZIO's service pattern

Both [[ZIOShowdownClient|io.github.pidoveproject.showdown.client.zio.ZIOShowdownClient]] and [[ZIOShowdownConnection|io.github.pidoveproject.showdown.client.zio.ZIOShowdownConnection]]'s companion object provide service accessors to be used with [ZIO's service pattern](https://zio.dev/reference/service-pattern/introduction).

Here is the example above rewritten using the service pattern:

```scala
val connectionProgram: ZIO[ZIOShowdownConnection, Throwable, Unit] =
  ZIOShowdownConnection
    .serverMessages
    .runForeachWhile:
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

val clientProgram: ZIO[Scope & ZIOShowdownClient, Throwable, Unit] =
  ZIOShowdownClient
    .openConnection()
    .flatMap(connection => connectionProgram.provideSome[ZIOShowdownClient](ZLayer.succeed(connection)))

//Can be used in a ZIOApp
val program: Task[Unit] =
  ZIO.scoped(clientProgram).provide(
    Client.default,
    ZIOShowdownClient.layer
  )
```

Note: authentication process has been truncated for readability reasons. Check [this page](../authentication.md) instead.