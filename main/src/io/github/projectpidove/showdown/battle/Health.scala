package io.github.projectpidove.showdown.battle

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.numeric.{GreaterEqual, Interval}
import io.github.projectpidove.showdown.protocol.ProtocolError
import io.github.projectpidove.showdown.protocol.MessageDecoder.toInvalidInput

case class Health(current: Int :| GreaterEqual[0], max: Int :| GreaterEqual[0])

object Health:

  def percent(current: Int :| Interval.Closed[0, 100]): Health = Health(current, 100)

  def fromString(value: String): Either[ProtocolError, Health] = value match
    case s"$current/$max" =>
      for
        intCurrent <- current.toIntOption.toRight(ProtocolError.InvalidInput(current, "Invalid int"))
        intMax <- max.toIntOption.toRight(ProtocolError.InvalidInput(max, "Invalid int"))
        validCurrent <- intCurrent.refineEither[GreaterEqual[0]].toInvalidInput(current)
        validMax <- intMax.refineEither[GreaterEqual[0]].toInvalidInput(max)
      yield
        Health(validCurrent, validMax)

    case health =>
      for
        intHealth <- health.toIntOption.toRight(ProtocolError.InvalidInput(health, "Invalid int"))
        validHealth <- intHealth.refineEither[Interval.Closed[0, 100]].toInvalidInput(health)
      yield
        Health.percent(validHealth)
