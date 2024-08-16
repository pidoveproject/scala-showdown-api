package io.github.pidoveproject.showdown.battle

import io.github.iltotore.iron.*
import io.github.pidoveproject.showdown.protocol.{MessageDecoder, ProtocolError}
import io.github.pidoveproject.showdown.protocol.MessageDecoder.toInvalidInput
import zio.json.JsonDecoder

/**
 * The health and status of a pokemon.
 *
 * @param health the health of the pokemon
 * @param status the status of the pokemon or `None` if healthy.
 */
case class Condition(health: Health, status: Option[StatusEffect] = None):

  /**
   * Whether this pokemon is fainted or not.
   */
  def fainted: Boolean = status.contains(StatusEffect.Fainted)

  /**
   * Set the statut as fainted.
   */
  def faint: Condition = this.copy(status = Some(StatusEffect.Fainted))

object Condition:

  /**
   * A healthy, full HP pokemon.
   */
  val Healthy: Condition = Condition(Health.percent(100))

  /**
   * Parse the pokemon condition from [[String]].
   *
   * @param value the text to parse
   * @return the read pokemon condition or a [[ProtocolError]] if it failed
   */
  def fromString(value: String): Either[ProtocolError, Condition] = value match
    case s"$healthValue $statusValue" =>
      for
        health <- Health.fromString(healthValue)
        status <- StatusEffect.either(statusValue).toInvalidInput(statusValue)
      yield Condition(health, Some(status))

    case _ =>
      Health.fromString(value).map(Condition(_))

  given MessageDecoder[Condition] = MessageDecoder.string.mapEither(fromString)

  given JsonDecoder[Condition] = JsonDecoder.string.mapOrFail(fromString(_).left.map(_.getMessage))
