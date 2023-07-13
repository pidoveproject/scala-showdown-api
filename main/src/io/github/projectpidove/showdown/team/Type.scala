package io.github.projectpidove.showdown.team

import zio.json.JsonCodec

enum Type:
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

  given JsonCodec[Type] = JsonCodec.string.transformOrFail(
    name => Type.values.find(_.toString == name).toRight(s"Invalid type: $name"),
    _.toString
  )
