package io.github.pidoveproject.showdown.battle

import io.github.pidoveproject.showdown.team.Surname
import zio.json.*

/**
 * A member identifier relative to a team.
 * 
 * @param player the owner
 * @param name the name of the pokemon
 */
case class TeamId(player: PlayerNumber, name: Surname)

object TeamId:

  given JsonDecoder[TeamId] = JsonDecoder.string.mapOrFail:
    case s"$number: $name" =>
      for
        validNumber <- PlayerNumber.fromString(number).left.map(_.toString)
        validName <- Surname.either(name)
      yield
        TeamId(validNumber, validName)

    case value => Left(s"Invalid team position: $value")