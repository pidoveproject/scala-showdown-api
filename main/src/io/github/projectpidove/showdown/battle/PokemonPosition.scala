package io.github.projectpidove.showdown.battle

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.protocol.{MessageDecoder, ProtocolError}

import scala.quoted.Expr

case class PokemonPosition(player: PlayerNumber, slot: PokemonSlot)

object PokemonPosition:

  def single(player: PlayerNumber): PokemonPosition = PokemonPosition(player, PokemonSlot(0))
  
  def fromString(value: String): Either[ProtocolError, PokemonPosition] =
    val stringNumber = value.substring(0, 2)
    val slotCode = value.last

    for
      playerNumber <- PlayerNumber.fromString(stringNumber)
      slot <- PokemonSlot.fromCode(slotCode)
    yield
      PokemonPosition(playerNumber, slot)

  extension (context: StringContext)

    def pos(args: Any*): PokemonPosition = context.parts match
      case Seq(head) => PokemonPosition.fromString(head).fold(throw _, identity)
      case _ => throw IllegalArgumentException("Position cannot contain spaces")

  
  given MessageDecoder[PokemonPosition] = MessageDecoder.string.mapEither(fromString)