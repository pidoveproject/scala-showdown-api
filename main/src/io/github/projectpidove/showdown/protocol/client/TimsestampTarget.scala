package io.github.projectpidove.showdown.protocol.client

import io.github.projectpidove.showdown.protocol.{MessageEncoder, MessageName}

enum TimestampTarget derives MessageEncoder:
  case All
  case Lobby
  @MessageName("pms") case PrivateMessages
