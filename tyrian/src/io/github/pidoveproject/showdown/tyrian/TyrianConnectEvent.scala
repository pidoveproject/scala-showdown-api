package io.github.pidoveproject.showdown.tyrian

import cats.effect.Async
import io.github.pidoveproject.showdown.protocol.ProtocolError
import tyrian.websocket.WebSocketConnect

/**
 * A Tyrian event related to the state of the connection to the Showdown server.
 *
 * @tparam F the effect type of the Tyrian app
 */
enum TyrianConnectEvent[F[_]]:

  /**
   * A connection to a Showdown server has been opened.
   *
   * @param connection the established connection
   */
  case Open(connection: TyrianShowdownConnection[F])

  /**
   * A [[ProtocolError]] occurred.
   *
   * @param error the thrown error
   */
  case Error(error: ProtocolError)

object TyrianConnectEvent:

  /**
   * Create a connection event from Tyrian's WebSocket events.
   *
   * @param event the connect event from Tyrian's WebSocket API.
   * @tparam F the effect type of the Tyrian app
   * @return the connection event created from the input
   */
  def fromTyrian[F[_] : Async](event: WebSocketConnect[F]): TyrianConnectEvent[F] = event match
    case WebSocketConnect.Error(msg) => Error(ProtocolError.Miscellaneous(msg))
    case WebSocketConnect.Socket(webSocket) => Open(TyrianShowdownConnection(webSocket))
