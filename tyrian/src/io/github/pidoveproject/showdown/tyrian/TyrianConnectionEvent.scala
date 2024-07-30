package io.github.pidoveproject.showdown.tyrian

import io.github.pidoveproject.showdown.protocol.ProtocolError
import io.github.pidoveproject.showdown.protocol.server.ServerMessage

enum TyrianConnectionEvent[+E, +A]:
  case Open
  case Close(code: Int, reason: String)
  case Receive(message: A)
  case Error(error: E)
  case Heartbeat