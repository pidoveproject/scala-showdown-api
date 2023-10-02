package io.github.projectpidove.showdown.battle

import io.github.projectpidove.showdown.protocol.{MessageDecoder, ProtocolError}
import MessageDecoder.toInvalidInput
import io.github.projectpidove.showdown.team.Surname

/**
 * The identifier of an active pokemon.
 *
 * @param position the position of the pokemon on the battlefield
 * @param name the surname or species of the pokemon
 */
case class ActiveId(position: PokemonPosition, name: Surname)

object ActiveId:

  /**
   * Parse a [[String]] of the format `pos: surname`.
   *
   * @param value the text to parse
   * @return a new [[ActiveId]] read from the content
   */
  def fromString(value: String): Either[ProtocolError, ActiveId] = value match
    case s"$stringPos: $stringName" =>
      for
        position <- PokemonPosition.fromString(stringPos)
        surname <- Surname.either(stringName).toInvalidInput(stringName)
      yield
        ActiveId(position, surname)
  
    case _ => Left(ProtocolError.InvalidInput(value, "Invalid pokemon ID"))
  
  given MessageDecoder[ActiveId] = MessageDecoder.string.mapEither(fromString)
    