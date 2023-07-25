package io.github.projectpidove.showdown.team

import zio.json.*

enum StatType(val shortName: String):
  case Health extends StatType("HP")
  case Attack extends StatType("Atk")
  case Defense extends StatType("Def")
  case SpecialAttack extends StatType("SpA")
  case SpecialDefense extends StatType("SpD")
  case Speed extends StatType("Spe")

object StatType:

  def fromShortName(shortName: String): Option[StatType] = StatType.values.find(_.shortName.equalsIgnoreCase(shortName))

  given JsonCodec[StatType] = JsonCodec.string.transformOrFail(
    name => fromShortName(name).toRight(s"Invalid stat name: $name"),
    _.shortName
  )

  given JsonFieldEncoder[StatType] = JsonFieldEncoder.string.contramap(_.shortName)
  given JsonFieldDecoder[StatType] = JsonFieldDecoder.string.mapOrFail(name => fromShortName(name).toRight(s"Invalid stat name: $name"))
