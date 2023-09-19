package io.github.projectpidove.showdown.battle

import io.github.projectpidove.showdown.protocol.{MessageDecoder, ProtocolError}
import io.github.projectpidove.showdown.protocol.MessageDecoder.toInvalidInput
import zio.json.JsonDecoder

case class HealthStatus(health: Health, status: Option[StatusEffect] = None)

object HealthStatus:

  def fromString(value: String): Either[ProtocolError, HealthStatus] = value match
    case s"$healthValue $statusValue" =>
      for
        health <- Health.fromString(healthValue)
        status <- StatusEffect.either(statusValue).toInvalidInput(statusValue)
      yield
        HealthStatus(health, Some(status))

    case _ =>
      Health.fromString(value).map(HealthStatus(_))

  given MessageDecoder[HealthStatus] = MessageDecoder.string.mapEither(fromString)

  given JsonDecoder[HealthStatus] = JsonDecoder.string.mapOrFail(fromString(_).left.map(_.getMessage))

