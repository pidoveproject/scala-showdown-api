package io.github.pidoveproject.showdown.cats

import cats.effect.{Concurrent, Resource}
import fs2.Stream
import io.github.pidoveproject.showdown.ShowdownClient
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.client.websocket.{WSClient, WSFrame, WSRequest}
import io.github.pidoveproject.showdown.protocol.URL
import io.github.iltotore.iron.autoRefine

class CatsShowdownClient[F[_]: Concurrent](httpClient: Client[F], wsClient: WSClient[F])
    extends ShowdownClient[WSFrame, [e, r] =>> F[r], [r] =>> Stream[F, r], [r] =>> Resource[F, r]]:

  override def openConnection(serverUrl: URL = URL("wss://sim3.psim.us/showdown/websocket")): Resource[F, CatsShowdownConnection[F]] =
    wsClient.connect(WSRequest(Uri.unsafeFromString(serverUrl.value))).map(CatsShowdownConnection(httpClient, _))
