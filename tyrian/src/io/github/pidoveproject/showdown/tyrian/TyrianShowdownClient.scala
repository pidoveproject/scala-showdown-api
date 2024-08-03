package io.github.pidoveproject.showdown.tyrian

import cats.Foldable
import cats.effect.Async
import io.github.pidoveproject.showdown.ShowdownClient
import tyrian.websocket.{WebSocket, WebSocketConnect}
import tyrian.{Cmd, Sub}

case class TyrianShowdownClient[F[_] : Async](serverUrl: String)
  extends ShowdownClient[String, [e, r] =>> Cmd[F, UnitToNothing[r]], [r] =>> Sub[F, TyrianConnectionEvent[r]], [r] =>> Cmd[F, Either[String, r]]]:

  override def openConnection: Cmd[F, Either[String, TyrianShowdownConnection[F]]] =
    WebSocket.connect(serverUrl):
      case WebSocketConnect.Socket(webSocket) => Right(TyrianShowdownConnection(webSocket))
      case WebSocketConnect.Error(error) => Left(error)

