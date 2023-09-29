package io.github.projectpidove.showdown.battle

import io.github.projectpidove.showdown.protocol.ProtocolError
import io.github.projectpidove.showdown.team.Surname
import zio.json.*

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