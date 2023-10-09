package io.github.projectpidove.showdown.client

import zio.ZIO
import zio.http.WebSocketFrame
import io.github.projectpidove.showdown.{ProtocolTask, ShowdownConnection}
import io.github.projectpidove.showdown.protocol.ProtocolError

type ConnectionTask[+A] = ZIO[ShowdownConnection[WebSocketFrame, ProtocolTask], ProtocolError, A]