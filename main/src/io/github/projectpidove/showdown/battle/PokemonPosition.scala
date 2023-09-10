package io.github.projectpidove.showdown.battle

import io.github.projectpidove.showdown.protocol.{MessageDecoder, ProtocolError}

case class PokemonPosition(player: PlayerNumber, slot: PokemonSlot)

object PokemonPosition:
  
  def fromString(value: String): Either[ProtocolError, PokemonPosition] =
    val stringNumber = value.substring(0, 1)
    val slotCode = value.last

    for
      playerNumber <- PlayerNumber.fromString(stringNumber)
      slot <- PokemonSlot.fromCode(slotCode)
    yield
      PokemonPosition(playerNumber, slot)
  
  given MessageDecoder[PokemonPosition] = MessageDecoder.string.mapEither(fromString)