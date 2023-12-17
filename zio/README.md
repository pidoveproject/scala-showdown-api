# Tyrian integration

This module provides support for [ZIO](https://zio.dev/), an effect system for Scala.

## Import

```scala
libraryDependencies += "io.github.projectpidove" %% "scala-showdown-api-zio" % "version"
```

Mill:

```scala
ivy"io.github.projectpidove::scala-showdown-api-zio:version"
```

## Usage

### Connecting to the server

The ZIO module provides two classes: [ZIOShowdownClient](https://pidove-project.github.io/scala-showdown-api/io/github/projectpidove/showdown/ZIOShowdownClient.html) and
[ZIOShowdownConnection](https://pidove-project.github.io/scala-showdown-api/io/github/projectpidove/showdown/ZIOShowdownClient.html).
You can use them as [ZIO services](https://zio.dev/reference/service-pattern/introduction/).

Here is the typical structure of a ZIO Showdown application:

```scala
import io.github.projectpidove.showdown.*
import zio.*
import zio.http.*

object Main extends ZIOAppDefault:

    //The program executed when a connection to a showdown server is opened
    def connectionProgram(connection: ShowdownConnection[WebSocketFrame, ProtocolTask]): ProtocolTask[Unit] =
      Console.printLine("Connected to server!")

    override def run =
      ZIOShowdownClient
        .openConnection(connectionProgram) //Open a connection
        .provide(
          Client.default, //ZIO-HTTP client
          ZIOShowdownClient.layer() //Showdown client (targeting the official server by default)
        )
```

You can check the [ZIO Client example](../examples/zio-client)

### Handling messages

Subscribing to messages is similar to opening a connection. You need to pass as an argument a program that will take
a message and return nothing.

```scala
//Program executed for each message
def subscribeProgram(message: ServerMessage): ProtocolTask[Unit] =
  Console.printLine(s"Received message: $message")

def connectionProgram(connection: ShowdownConnection[WebSocketFrame, ProtocolTask]): ProtocolTask[Unit] =
  connection.subscribe(subscribeProgram)
```

### Going further

Check the [Scaladoc](https://pidove-project.github.io/scala-showdown-api/io/github/projectpidove/showdown.html)
for further methods.