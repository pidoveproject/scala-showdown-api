package io.github.pidoveproject.showdown.client

import zio.{IO, ZIO}
import zio.http.WebSocketFrame
import zio.stream.Stream
import io.github.pidoveproject.showdown.ShowdownConnection
import io.github.pidoveproject.showdown.protocol.ProtocolError
import io.github.pidoveproject.showdown.{ZIOShowdownClient, ZIOShowdownConnection}

/**
 * A protocol task depending on a [[ShowdownConnection]].
 * 
 * @tparam A the return type of this program
 */
type ConnectionTask[+A] = ZIO[ZIOShowdownClient & ZIOShowdownConnection, ProtocolError, A]