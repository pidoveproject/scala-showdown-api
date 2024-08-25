package io.github.pidoveproject.showdown.client.sync

import io.github.pidoveproject.showdown.client.zio.ZIOShowdownClient
import io.github.pidoveproject.showdown.client.ShowdownClient
import io.github.pidoveproject.showdown.protocol.URL
import io.github.pidoveproject.showdown.ChallStr
import io.github.pidoveproject.showdown.protocol.LoginResponse
import io.github.pidoveproject.showdown.user.Username
import io.github.pidoveproject.showdown.protocol.Assertion
import zio.*
import zio.http.ZClient
import io.github.pidoveproject.showdown.client.zio.ZIOShowdownConnection
import io.github.iltotore.iron.*

class SyncShowdownClient(runtime: Runtime[ZIOShowdownClient])
    extends ShowdownClient[String, [e, r] =>> r, [r] =>> PartialFunction[r, Boolean] => Unit, [r] =>> (r => Unit) => Unit]:

  override def openConnection(serverUrl: URL = URL("wss://sim3.psim.us/showdown/websocket")): (SyncShowdownConnection => Unit) => Unit = f =>
    ZIO.scoped(
      ZIOShowdownClient
        .openConnection(serverUrl)
        .flatMap(ZLayer.succeed(_).toRuntime)
        .map(SyncShowdownConnection.apply)
        .mapAttempt(f)
    ).runThrowFailure(runtime)

  override def login(challStr: ChallStr)(name: Username, password: String): LoginResponse =
    ZIOShowdownClient.login(challStr)(name, password).runThrowFailure(runtime)

  override def loginGuest(challStr: ChallStr)(name: Username): Assertion =
    ZIOShowdownClient.loginGuest(challStr)(name).runThrowFailure(runtime)

object SyncShowdownClient:

  private val defaultRuntime: ZIO[Scope, Throwable, Runtime[ZIOShowdownClient]] =
    ZClient
      .default
      .to(ZIOShowdownClient.layer)
      .toRuntime

  def use(f: SyncShowdownClient => Unit): Unit =
    ZIO
      .scoped(defaultRuntime.mapAttempt(r => f(SyncShowdownClient(r))))
      .runThrowFailure(Runtime.default)

  def connect(serverUrl: URL = URL("wss://sim3.psim.us/showdown/websocket"))(f: (SyncShowdownClient, SyncShowdownConnection) => Unit): Unit =
    use(client =>
      client.openConnection(serverUrl)(connection => f(client, connection))
    )
