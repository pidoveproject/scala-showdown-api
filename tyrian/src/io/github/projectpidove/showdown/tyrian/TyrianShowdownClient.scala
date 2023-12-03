package io.github.projectpidove.showdown.tyrian

import cats.effect.kernel.Async
import tyrian.Cmd
import tyrian.websocket.WebSocket

/**
 * The Showdown client for Tyrian, inspired by Tyrian's WebSocket API (and using it internally).
 */
object TyrianShowdownClient:

  /**
   * Connect to a Pokemon Showdown server.
   *
   * @param url the url of the Showdown server
   * @tparam F the effect type used by the Tyrian app
   * @return a new Tyrian task returning a [[TyrianConnectEvent]]
   */
  def openConnection[F[_] : Async](url: String): Cmd[F, TyrianConnectEvent[F]] = WebSocket.connect(url)(TyrianConnectEvent.fromTyrian)