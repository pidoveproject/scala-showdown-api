package io.github.projectpidove.showdown.tyrian

import cats.effect.Async
import io.github.projectpidove.showdown.protocol.ProtocolError
import tyrian.websocket.WebSocketConnect

enum TyrianConnectEvent[F[_]]:
  case Open(connection: TyrianShowdownConnection[F])
  case Error(error: ProtocolError)

object TyrianConnectEvent:

  def fromTyrian[F[_] : Async](event: WebSocketConnect[F]): TyrianConnectEvent[F] = event match
    case WebSocketConnect.Error(msg) => Error(ProtocolError.Miscellaneous(msg))
    case WebSocketConnect.Socket(webSocket) => Open(TyrianShowdownConnection(webSocket))
