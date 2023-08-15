package io.github.projectpidove.showdown.protocol

enum ProtocolError:
  case InputExhausted(data: String, length: Int)
  case InvalidInput(input: String, message: String)
  case InvalidFormat(message: String)
  case Miscellaneous(message: String)
  case Thrown(cause: Throwable)
