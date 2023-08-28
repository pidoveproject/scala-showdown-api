package io.github.projectpidove.showdown.protocol.client

import io.github.projectpidove.showdown.protocol.MessageEncoder

enum GroupTarget derives MessageEncoder:
  case Global
  case Room
