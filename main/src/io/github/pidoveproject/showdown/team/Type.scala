package io.github.pidoveproject.showdown.team

import io.github.pidoveproject.showdown.protocol.{MessageDecoder, MessageEncoder, ProtocolError}
import zio.json.JsonCodec

/**
 * The pokemon types.
 */
enum Type derives MessageEncoder: // TODO convert it to a case class since the data can be dynamic
  case Bug
  case Dragon
  case Electric
  case Fighting
  case Fire
  case Flying
  case Grass
  case Ground
  case Ice
  case Normal
  case Poison
  case Psychic
  case Rock
  case Water
  case Dark
  case Fairy
  case Ghost
  case Steel

object Type:

  def fromName(name: String): Option[Type] =
    Type.values.find(_.toString == name)

  given JsonCodec[Type] = JsonCodec.string.transformOrFail(
    name => fromName(name).toRight(s"Invalid type: $name"),
    _.toString
  )

  given MessageDecoder[Type] =
    MessageDecoder
      .string
      .mapEither(x => fromName(x).toRight(ProtocolError.InvalidInput(x, "Invalid type")))
