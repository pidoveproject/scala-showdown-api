package io.github.pidoveproject.showdown.tyrian

import cats.effect.Async
import io.github.pidoveproject.showdown.ShowdownClient
import io.github.pidoveproject.showdown.protocol.URL
import io.github.iltotore.iron.autoRefine
import tyrian.websocket.{WebSocket, WebSocketConnect}
import tyrian.{Cmd, Sub}

class TyrianShowdownClient[F[_]: Async]
    extends ShowdownClient[String, [e, r] =>> Cmd[F, UnitToNothing[r]], [r] =>> Sub[F, TyrianConnectionEvent[r]], [r] =>> Cmd[F, Either[String, r]]]:

  override def openConnection(serverUrl: URL = URL("wss://sim3.psim.us/showdown/websocket")): Cmd[F, Either[String, TyrianShowdownConnection[F]]] =
    WebSocket.connect(serverUrl.value):
      case WebSocketConnect.Socket(webSocket) => Right(TyrianShowdownConnection(webSocket))
      case WebSocketConnect.Error(error)      => Left(error)
