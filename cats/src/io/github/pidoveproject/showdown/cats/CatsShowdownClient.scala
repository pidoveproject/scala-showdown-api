package io.github.pidoveproject.showdown.cats

import cats.effect.{Concurrent, Resource}
import fs2.Stream
import io.github.pidoveproject.showdown.ShowdownClient
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.client.websocket.{WSClient, WSFrame, WSRequest}

class CatsShowdownClient[F[_] : Concurrent](httpClient: Client[F], wsClient: WSClient[F], serverUri: Uri)
  extends ShowdownClient[WSFrame, [e, r] =>> F[r], [r] =>> Stream[F, r], [r] =>> Resource[F, r]]:

  override def openConnection: Resource[F, CatsShowdownConnection[F]] =
    wsClient.connect(WSRequest(serverUri)).map(CatsShowdownConnection(httpClient, _))
