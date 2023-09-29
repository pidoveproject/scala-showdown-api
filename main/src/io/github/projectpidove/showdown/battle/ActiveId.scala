package io.github.projectpidove.showdown.battle

import io.github.projectpidove.showdown.protocol.{MessageDecoder, ProtocolError}
import MessageDecoder.toInvalidInput
import io.github.projectpidove.showdown.team.Surname

case class ActiveId(position: PokemonPosition, name: Surname)

object ActiveId:
  
  given MessageDecoder[ActiveId] = MessageDecoder.string.mapEither:
    case s"$stringPos: $stringName" =>
      for
        position <- PokemonPosition.fromString(stringPos)
        surname <- Surname.either(stringName).toInvalidInput(stringName)
      yield
        ActiveId(position, surname)

    case value => Left(ProtocolError.InvalidInput(value, "Invalid pokemon ID"))