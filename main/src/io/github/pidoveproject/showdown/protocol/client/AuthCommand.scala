package io.github.pidoveproject.showdown.protocol.client

import io.github.pidoveproject.showdown.protocol.MessageEncoder
import io.github.pidoveproject.showdown.user.Username

/**
 * An authentification-related command
 */
enum AuthCommand derives MessageEncoder:
  case Trn(name: Username, data: Int, assertion: String)
