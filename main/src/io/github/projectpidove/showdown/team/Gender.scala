package io.github.projectpidove.showdown.team

import zio.json.*

/**
 * The gender of a pokemon. Possibly genderless fields are represented using an `Option`.
 * @param shortName the abbreviated name of the gender
 */
enum Gender(val shortName: String):
  case Male extends Gender("M")
  case Female extends Gender("F")

object Gender:

  /**
   * Get a gender from its short name.
   *
   * @param shortName the short name of the gender
   * @return [[Male]] if `shortName` is `M`, [[Female]] if it is `F`
   */
  def fromShortName(shortName: String): Option[Gender] = Gender.values.find(_.shortName == shortName)

  given JsonCodec[Gender] = JsonCodec.string.transformOrFail(
    name => fromShortName(name).toRight(s"Invalid gender: $name"),
    _.shortName
  )
