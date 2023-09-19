package io.github.projectpidove.showdown.battle

import io.github.projectpidove.showdown.protocol.ProtocolError
import io.github.projectpidove.showdown.team.Surname
import zio.json.*

case class TeamPosition(player: PlayerNumber, name: Surname)

object TeamPosition:

  given JsonDecoder[TeamPosition] = JsonDecoder.string.mapOrFail:
    case s"$number: $name" =>
      for
        validNumber <- PlayerNumber.fromString(number).left.map(_.toString)
        validName <- Surname.either(name)
      yield
        TeamPosition(validNumber, validName)

    case value => Left(s"Invalid team position: $value")