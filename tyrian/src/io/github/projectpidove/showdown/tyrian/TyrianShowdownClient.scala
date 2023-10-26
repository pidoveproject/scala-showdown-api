package io.github.projectpidove.showdown.tyrian

import cats.effect.kernel.Async
import tyrian.Cmd
import tyrian.websocket.WebSocket

object TyrianShowdownClient:

  def openConnection[F[_] : Async](url: String): Cmd[F, TyrianConnectEvent[F]] = WebSocket.connect(url)(TyrianConnectEvent.fromTyrian)