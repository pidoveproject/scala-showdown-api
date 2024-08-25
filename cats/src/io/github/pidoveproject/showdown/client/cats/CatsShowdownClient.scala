package io.github.pidoveproject.showdown.client.cats

import zio.Runtime
import zio.interop.ToEffectSyntax
import zio.interop.catz.scopedSyntax
import io.github.pidoveproject.showdown.client.zio.ZIOShowdownClient
import io.github.pidoveproject.showdown.client.ShowdownClient
import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import fs2.Stream
import io.github.pidoveproject.showdown.protocol.URL
import zio.ZLayer
import io.github.pidoveproject.showdown.ChallStr
import io.github.pidoveproject.showdown.protocol.LoginResponse
import io.github.pidoveproject.showdown.user.Username
import io.github.pidoveproject.showdown.protocol.Assertion

class CatsShowdownClient[F[_]: Async](runtime: Runtime[ZIOShowdownClient])
    extends ShowdownClient[String, [e, r] =>> F[r], [r] =>> Stream[F, r], [r] =>> Resource[F, r]]:

  private given Runtime[ZIOShowdownClient] = runtime

  override def openConnection(serverUrl: URL): Resource[F, CatsShowdownConnection[F]] =
    Resource.scoped[F, ZIOShowdownClient, CatsShowdownConnection[F]](
      ZIOShowdownClient
        .openConnection(serverUrl)
        .flatMap(ZLayer.succeed(_).toRuntime)
        .map(CatsShowdownConnection(_))
    )

  override def login(challStr: ChallStr)(name: Username, password: String): F[LoginResponse] =
    ZIOShowdownClient.login(challStr)(name, password).toEffect[F]

  override def loginGuest(challStr: ChallStr)(name: Username): F[Assertion] =
    ZIOShowdownClient.loginGuest(challStr)(name).toEffect[F]
