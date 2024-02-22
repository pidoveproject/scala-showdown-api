package io.github.pidoveproject.showdown.battle

import io.github.pidoveproject.showdown.protocol.{MessageDecoder, MessageEncoder, ProtocolError}
import io.github.pidoveproject.showdown.protocol.MessageDecoder.toInvalidInput

/**
 * The relative position of an active pokemon.
 * 
 * @param side the relative side of the pokemon
 * @param slot the slot of the pokemon
 */
case class RelativePosition(side: RelativeSide, slot: PokemonSlot)

object RelativePosition:

  given MessageEncoder[RelativePosition] = MessageEncoder.string.contramap: target =>
    val side =
      if target.side == RelativeSide.Ally then "-"
      else "+"

    s"$side${target.slot}"

  given MessageDecoder[RelativePosition] = MessageDecoder.string.mapEither:
    case s"+$slot" =>
      for
        intSlot <- slot.toIntOption.toRight(ProtocolError.InvalidInput(slot, "Invalid int"))
        pokemonSlot <- PokemonSlot.either(intSlot).toInvalidInput(slot)
      yield
        RelativePosition(RelativeSide.Enemy, pokemonSlot)

    case s"-$slot" =>
      for
        intSlot <- slot.toIntOption.toRight(ProtocolError.InvalidInput(slot, "Invalid int"))
        pokemonSlot <- PokemonSlot.either(intSlot).toInvalidInput(slot)
      yield
        RelativePosition(RelativeSide.Ally, pokemonSlot)

    case value => Left(ProtocolError.InvalidInput(value, "Invalid relative position"))