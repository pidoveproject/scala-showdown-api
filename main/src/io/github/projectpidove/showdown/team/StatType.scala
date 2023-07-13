package io.github.projectpidove.showdown.team

import zio.json.*

enum StatType(val shortName: String):
  case Health extends StatType("hp")
  case Attack extends StatType("atk")
  case Defense extends StatType("def")
  case SpecialAttack extends StatType("spa")
  case SpecialDefense extends StatType("spd")
  case Speed extends StatType("spe")

object StatType:

  def fromShortName(shortName: String): Option[StatType] = StatType.values.find(_.shortName.equalsIgnoreCase(shortName))

  given JsonCodec[StatType] = JsonCodec.string.transformOrFail(
    name => fromShortName(name).toRight(s"Invalid stat name: $name"),
    _.shortName
  )

  given JsonFieldEncoder[StatType] = JsonFieldEncoder.string.contramap(_.shortName)
  given JsonFieldDecoder[StatType] = JsonFieldDecoder.string.mapOrFail(name => fromShortName(name).toRight(s"Invalid stat name: $name"))
