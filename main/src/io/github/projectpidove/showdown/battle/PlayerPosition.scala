package io.github.projectpidove.showdown.battle

import io.github.projectpidove.showdown.protocol.{MessageDecoder, ProtocolError}
import io.github.projectpidove.showdown.protocol.MessageDecoder.toInvalidInput
import io.github.projectpidove.showdown.user.Username

case class PlayerPosition(number: PlayerNumber, name: Username)

object PlayerPosition:

  given MessageDecoder[PlayerPosition] = MessageDecoder.string.mapEither:
    case s"$number: $name" =>
      for
        validNumber <- PlayerNumber.fromString(number)
        validName <- Username.either(name).toInvalidInput(name)
      yield
        PlayerPosition(validNumber, validName)

    case value => Left(ProtocolError.InvalidInput(value, "Invalid player position"))