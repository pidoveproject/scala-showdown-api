package io.github.projectpidove.showdown.battle

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.protocol.{MessageDecoder, ProtocolError}

import scala.quoted.Expr

/**
 * The position of an active pokemon.
 * 
 * @param player the side of the pokemon
 * @param slot the slot of the pokemon in its side
 */
case class ActivePosition(player: PlayerNumber, slot: PokemonSlot)

object ActivePosition:

  /**
   * Create a pokemon position in singles.
   * 
   * @param player the side of the pokemon
   * @return a position representing the first slot of the given side
   */
  def single(player: PlayerNumber): ActivePosition = ActivePosition(player, PokemonSlot(0))

  /**
   * Parse a position from a [[String]].
   * 
   * @param value the text to parse
   * @return the parsed position or a [[ProtocolError]] if it failed.
   */
  def fromString(value: String): Either[ProtocolError, ActivePosition] =
    val stringNumber = value.substring(0, 2)
    val slotCode = value.last

    for
      playerNumber <- PlayerNumber.fromString(stringNumber)
      slot <- PokemonSlot.fromCode(slotCode)
    yield
      ActivePosition(playerNumber, slot)

  extension (context: StringContext)

    /**
     * Interpolator to create a position.
     */
    def pos(args: Any*): ActivePosition = context.parts match
      case Seq(head) => ActivePosition.fromString(head).fold(throw _, identity)
      case _ => throw IllegalArgumentException("Position cannot contain spaces")

  
  given MessageDecoder[ActivePosition] = MessageDecoder.string.mapEither(fromString)