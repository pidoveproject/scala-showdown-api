# Tyrian integration

This module provides support for [Tyrian](https://tyrian.indigoengine.io/), a GUI library
for [Scala.js](https://scala-js.org/) inspired by the Elm architecture.

## Import

```scala
libraryDependencies += "io.github.projectpidove" %%% "scala-showdown-api-tyrian" % "version"
```

Mill:

```scala
ivy"io.github.projectpidove::scala-showdown-api-tyrian::version"
```

Note: this library is only available for Scala.js

## Usage

### Connecting to the server

You can connect to a showdown instance using
the [openConnection](https://scala-showdown-api.pidove-project.github.io/io/github/projectpidove/showdown/tyrian/TyrianShowdownClient$.html#openConnection-f42)
method:

```scala
case class ClientApp()

def init(flags: Map[String, String]): (ClientApp, Cmd[IO, TyrianShowdownEvent[IO]]) =
    (ClientApp(), TyrianShowdownClient.openConnection[IO]("wss://sim3.psim.us/showdown/websocket"))
```

The executed [Cmd](https://tyrian.indigoengine.io/api/tyrian/Cmd.html) returns a
[TyrianConnectEvent](https://scala-showdown-api.pidove-project.github.io/io/github/projectpidove/showdown/tyrian/TyrianConnectEvent.html).
The returned event contains, if successful, a
[TyrianShowdownConnection](https://scala-showdown-api.pidove-project.github.io/io/github/projectpidove/showdown/tyrian/TyrianShowdownConnection.html)
which has a similar API than a "normal"
[ShowdownConnection](https://scala-showdown-api.pidove-project.github.io/io/github/projectpidove/showdown/ShowdownConnection.html).

### Handling messages

You can subscribe to Showdown message. Using [Tyrian's subscription system](https://tyrian.indigoengine.io/02-guides/subs/).

```scala
//eventToMessage: TyrianServerEvent => Msg
//connection: TyrianShowdownConnection
connection.subscribe(eventToMessage)
```

Then, you will receive events as messages.

Example taken from [Tyrian client demo](../examples/tyrian-client):

```scala
def update(app: ClientApp): ClientMessage => (ClientApp, Cmd[IO, ClientMessage]) =
  //...
  case ClientMessage.ShowdownEvent(TyrianServerEvent.Receive(messages)) => ???
```

### Going further

Check the [Scaladoc](https://pidove-project.github.io/scala-showdown-api/io/github/projectpidove/showdown/tyrian.html)
for further methods.
