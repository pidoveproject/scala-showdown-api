package io.github.projectpidove.showdown.protocol.client

import io.github.projectpidove.showdown.protocol.MessageEncoder

enum TimeInterval derives MessageEncoder:
  case Minutes()
  case Seconds()
  case Off()
