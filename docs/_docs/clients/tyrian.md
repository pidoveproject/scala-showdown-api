---
title: Tyrian
---

# Tyrian

A [Tyrian](https://tyrian.indigoengine.io/)-based implementation implementation of [[ShowdownClient|io.github.pidoveproject.showdown.client.ShowdownClient]].

## Connecting to the server

You can use [[TyrianShowdownClient#openConnection|io.github.pidoveproject.showdown.tyrian.TyrianShowdownClient.openConnection]] to connect to the Pokemon Showdown server. It uses [Tyrian's Cmd system](https://tyrian.indigoengine.io/02-guides/cmd/.)

```scala
case class Model(connection: Option[TyrianShowdownConnection[F]])
```

```scala
enum Msg:
  case Connected(connection: Either[String, TyrianShowdownConnection[F]])
  case ShowdownEvent(event: TyrianConnectionEvent[Either[ProtocolError, ServerMessage]])
```

```scala
object Main extends TyrianAppF[F, Msg, Model]:

  def init(flags: Map[String, String]): (Model, Cmd[F, Msg]) =
    (
      Model.init,
      TyrianShowdownConnection[F].openConnection().map(Connected.apply)
    )

  def update(model: Model): Msg => (Model, Cmd[F, Model]) =
    case Connected(Right(connection)) => (model.copy(connection = Some(connection)), Cmd.None)
    case Connected(Left(error)) =>
      println(s"Error: $error")
      (model, Cmd.None)
      
    case ShowdownEvent(_) => (model, Cmd.None)
```

## Receiving messages

[[Socket events|io.github.pidoveproject.showdown.tyrian.TyrianConnectionEvent]], including messages, can be received using [Tyrian's subscription system](https://tyrian.indigoengine.io/02-guides/subs/).

```scala
def subscriptions(model: Model): Sub[F, Msg] =
  model.connection.fold(Sub.None)(_.serverMessages.map(Msg.ShowdownEvent.apply))
```