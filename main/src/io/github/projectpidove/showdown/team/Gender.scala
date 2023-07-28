package io.github.projectpidove.showdown.team

import zio.json.*

enum Gender(val shortName: String):
  case Male extends Gender("M")
  case Female extends Gender("F")

object Gender:

  def fromShortName(shortName: String): Option[Gender] = Gender.values.find(_.shortName == shortName)

  given JsonCodec[Gender] = JsonCodec.string.transformOrFail(
    name => fromShortName(name).toRight(s"Invalid gender: $name"),
    _.shortName
  )
