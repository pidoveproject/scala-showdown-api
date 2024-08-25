package io.github.pidoveproject.showdown.client.cats

import cats.effect.kernel.Async
import fs2.Stream
import io.github.pidoveproject.showdown.client.zio.ZIOShowdownConnection
import io.github.pidoveproject.showdown.client.ShowdownConnection
import io.github.pidoveproject.showdown.protocol.ProtocolError
import io.github.pidoveproject.showdown.protocol.server.ServerMessage
import io.github.pidoveproject.showdown.protocol.client.ClientMessage
import io.github.pidoveproject.showdown.room.RoomId
import zio.Runtime
import zio.http.WebSocketFrame
import zio.interop.ToEffectSyntax
import zio.stream.interop.fs2z.zStreamSyntax
import zio.ZIO
import cats.arrow.FunctionK

class CatsShowdownConnection[F[_]: Async](runtime: Runtime[ZIOShowdownConnection])
    extends ShowdownConnection[String, [e, r] =>> F[r], [r] =>> Stream[F, r]]:

  private given Runtime[ZIOShowdownConnection] = runtime

  override def sendRawMessage(message: String): F[Unit] =
    ZIOShowdownConnection.sendRawMessage(WebSocketFrame.text(message)).toEffect[F]

  override def sendMessage(room: RoomId, message: ClientMessage): F[Unit] =
    ZIOShowdownConnection.sendMessage(room, message).toEffect[F]

  override def sendMessage(message: ClientMessage): F[Unit] =
    ZIOShowdownConnection.sendMessage(message).toEffect[F]

  override def disconnect(): F[Unit] = ZIOShowdownConnection.disconnect().toEffect[F]

  override def serverMessages: Stream[F, Either[ProtocolError, ServerMessage]] =
    ZIOShowdownConnection
      .serverMessages
      .toFs2Stream
      .translate(new FunctionK[[x] =>> ZIO[ZIOShowdownConnection, Throwable, x], F]:
        override def apply[A](fa: ZIO[ZIOShowdownConnection, Throwable, A]): F[A] = fa.toEffect[F]
      )
