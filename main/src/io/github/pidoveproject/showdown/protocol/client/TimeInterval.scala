package io.github.pidoveproject.showdown.protocol.client

import io.github.pidoveproject.showdown.protocol.MessageEncoder

/**
 * An interval of time.
 */
enum TimeInterval derives MessageEncoder:
  case Minutes
  case Seconds
  case Off
