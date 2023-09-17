package io.github.projectpidove.showdown.battle

import io.github.projectpidove.showdown.protocol.MessageDecoder
import io.github.projectpidove.showdown.protocol.MessageDecoder.toInvalidInput

case class HealthStatus(health: Health, status: Option[Status] = None)

object HealthStatus:

  given MessageDecoder[HealthStatus] = MessageDecoder.string.mapEither:
    case s"$healthValue $statusValue" =>
      for
        health <- Health.fromString(healthValue)
        status <- Status.either(statusValue).toInvalidInput(statusValue)
      yield
        HealthStatus(health, Some(status))

    case healthValue =>
      Health.fromString(healthValue).map(HealthStatus(_))
