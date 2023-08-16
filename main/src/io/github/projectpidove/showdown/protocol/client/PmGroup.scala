package io.github.projectpidove.showdown.protocol.client

import io.github.projectpidove.showdown.protocol.{MessageEncoder, MessageName}

enum PmGroup derives MessageEncoder:
  case Unlocked()
  case Ac()
  case Trusted()
  @MessageName("+") case Plus()
  case Friends()
