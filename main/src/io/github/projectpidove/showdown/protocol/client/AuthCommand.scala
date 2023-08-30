package io.github.projectpidove.showdown.protocol.client

import io.github.projectpidove.showdown.protocol.MessageEncoder
import io.github.projectpidove.showdown.user.Username

/**
 * An authentification-related command
 */
enum AuthCommand derives MessageEncoder:
  case Trn(name: Username, data: Int, assertion: String)