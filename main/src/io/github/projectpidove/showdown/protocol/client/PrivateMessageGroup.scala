package io.github.projectpidove.showdown.protocol.client

import io.github.projectpidove.showdown.protocol.{MessageEncoder, messageName}

/**
 * A group of user that can send private messages.
 */
enum PrivateMessageGroup derives MessageEncoder:
  case Unlocked
  case Ac
  case Trusted
  @messageName("+") case Plus
  case Friends
