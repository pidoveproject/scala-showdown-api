package io.github.pidoveproject.showdown.client

import _root_.zio.{IO, ZIO}
import _root_.zio.http.WebSocketFrame
import _root_.zio.stream.Stream
import io.github.pidoveproject.showdown.ShowdownConnection
import io.github.pidoveproject.showdown.protocol.ProtocolError
import io.github.pidoveproject.showdown.client.zio.{ZIOShowdownClient, ZIOShowdownConnection}

/**
 * A protocol task depending on a [[ShowdownConnection]].
 * 
 * @tparam A the return type of this program
 */
type ConnectionTask[+A] = ZIO[ZIOShowdownClient & ZIOShowdownConnection, ProtocolError, A]