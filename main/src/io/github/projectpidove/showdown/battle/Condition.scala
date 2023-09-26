package io.github.projectpidove.showdown.battle

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.protocol.{MessageDecoder, ProtocolError}
import io.github.projectpidove.showdown.protocol.MessageDecoder.toInvalidInput
import zio.json.JsonDecoder

case class Condition(health: Health, status: Option[StatusEffect] = None):
  
  def fainted: Boolean = status.contains(StatusEffect.Fainted)
  
  def faint: Condition = this.copy(status = Some(StatusEffect.Fainted))

object Condition:

  val Healthy: Condition = Condition(Health.percent(100))

  def fromString(value: String): Either[ProtocolError, Condition] = value match
    case s"$healthValue $statusValue" =>
      for
        health <- Health.fromString(healthValue)
        status <- StatusEffect.either(statusValue).toInvalidInput(statusValue)
      yield
        Condition(health, Some(status))

    case _ =>
      Health.fromString(value).map(Condition(_))

  given MessageDecoder[Condition] = MessageDecoder.string.mapEither(fromString)

  given JsonDecoder[Condition] = JsonDecoder.string.mapOrFail(fromString(_).left.map(_.getMessage))

