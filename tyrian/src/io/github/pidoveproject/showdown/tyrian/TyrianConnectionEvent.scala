package io.github.pidoveproject.showdown.tyrian

enum TyrianConnectionEvent[+A]:
  case Open
  case Close(code: Int, reason: String)
  case Receive(messages: List[A])
  case Error(error: String)
  case Heartbeat
