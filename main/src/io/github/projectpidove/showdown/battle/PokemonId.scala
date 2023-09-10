package io.github.projectpidove.showdown.battle

import io.github.projectpidove.showdown.protocol.{MessageDecoder, ProtocolError}
import MessageDecoder.toInvalidInput
import io.github.projectpidove.showdown.team.Surname

case class PokemonId(position: PokemonPosition, name: Surname)

object PokemonId:
  
  given MessageDecoder[PokemonId] = MessageDecoder.string.mapEither:
    case s"$stringPos: $stringName" =>
      for
        position <- PokemonPosition.fromString(stringPos)
        surname <- Surname.either(stringName).toInvalidInput(stringName)
      yield
        PokemonId(position, surname)

    case value => Left(ProtocolError.InvalidInput(value, "Invalid pokemon ID"))
