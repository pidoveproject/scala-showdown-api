package io.github.pidoveproject.showdown.team

import io.github.pidoveproject.showdown.protocol.{MessageDecoder, ProtocolError}
import zio.json.*

/**
 * One of the six stat types.
 *
 * @param shortName the short name of this stat type
 */
enum StatType(val shortName: String):
  case Health extends StatType("HP")
  case Attack extends StatType("Atk")
  case Defense extends StatType("Def")
  case SpecialAttack extends StatType("SpA")
  case SpecialDefense extends StatType("SpD")
  case Speed extends StatType("Spe")

object StatType:

  /**
   * Get a stat type from its short name.
   *
   * @param shortName the short name of the stat type
   * @return the corresponding [[StatType]]
   */
  def fromShortName(shortName: String): Option[StatType] = StatType.values.find(_.shortName.equalsIgnoreCase(shortName))

  given JsonCodec[StatType] = JsonCodec.string.transformOrFail(
    name => fromShortName(name).toRight(s"Invalid stat name: $name"),
    _.shortName.toLowerCase
  )

  given JsonFieldEncoder[StatType] = JsonFieldEncoder.string.contramap(_.shortName.toLowerCase)
  given JsonFieldDecoder[StatType] = JsonFieldDecoder.string.mapOrFail(name => fromShortName(name).toRight(s"Invalid stat name: $name"))

  given MessageDecoder[StatType] = MessageDecoder.string.mapEither(name => fromShortName(name).toRight(ProtocolError.InvalidInput(name, "Invalid stat name")))