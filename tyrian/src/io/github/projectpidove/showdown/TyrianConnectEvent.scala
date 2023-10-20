package io.github.projectpidove.showdown

import cats.effect.Async
import io.github.projectpidove.showdown.protocol.ProtocolError
import tyrian.websocket.WebSocketConnect

enum TyrianConnectEvent[F[_]]:
  case Open(connection: TyrianConnection[F])
  case Error(error: ProtocolError)

object TyrianConnectEvent:

  def fromTyrian[F[_] : Async](event: WebSocketConnect[F]): TyrianConnectEvent[F] = event match
    case WebSocketConnect.Error(msg) => Error(ProtocolError.Miscellaneous(msg))
    case WebSocketConnect.Socket(webSocket) => Open(TyrianConnection(webSocket))
