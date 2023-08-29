package io.github.projectpidove.showdown.protocol.client

import io.github.projectpidove.showdown.protocol.MessageEncoder

/**
 * An interval of time.
 */
enum TimeInterval derives MessageEncoder:
  case Minutes
  case Seconds
  case Off
