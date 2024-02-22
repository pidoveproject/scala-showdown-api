package io.github.pidoveproject.showdown.battle

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.numeric.{GreaterEqual, Interval}
import io.github.pidoveproject.showdown.protocol.ProtocolError
import io.github.pidoveproject.showdown.protocol.MessageDecoder.toInvalidInput

/**
 * The health information of a pokemon.
 * 
 * @param current the current health of the pokemon
 * @param max the maximum health of the pokemon
 */
case class Health(current: Int :| GreaterEqual[0], max: Int :| GreaterEqual[0])

object Health:

  /**
   * Create health information from percentage.
   * 
   * @param current the current health of the pokemon, between 0 and 100
   * @return a new [[Health]] with max HP set to 100
   */
  def percent(current: Int :| Interval.Closed[0, 100]): Health = Health(current, 100)

  /**
   * Parse health information from a [[String]].
   * 
   * @param value the text to parse
   * @return the read instance or a [[ProtocolError]] if it failed
   */
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
