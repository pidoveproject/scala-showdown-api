package io.github.pidoveproject.showdown.battle

import io.github.pidoveproject.showdown.protocol.{MessageDecoder, ProtocolError}
import io.github.pidoveproject.showdown.protocol.MessageDecoder.toInvalidInput
import io.github.pidoveproject.showdown.user.Username

/**
 * The position of a player.
 *
 * @param number the player id
 * @param name the name of the player
 */
case class PlayerId(number: PlayerNumber, name: Username)

object PlayerId:

  given MessageDecoder[PlayerId] = MessageDecoder.string.mapEither:
    case s"$number: $name" =>
      for
        validNumber <- PlayerNumber.fromString(number)
        validName <- Username.either(name).toInvalidInput(name)
      yield
        PlayerId(validNumber, validName)

    case value => Left(ProtocolError.InvalidInput(value, "Invalid player position"))