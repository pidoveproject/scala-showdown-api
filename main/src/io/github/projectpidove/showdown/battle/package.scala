package io.github.projectpidove.showdown.battle

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.projectpidove.showdown.protocol.ProtocolError
import io.github.projectpidove.showdown.protocol.MessageDecoder.toInvalidInput

opaque type PlayerNumber = Int :| Positive
object PlayerNumber extends RefinedTypeOpsImpl[Int, Positive, PlayerNumber]:

  def fromString(value: String): Either[ProtocolError, PlayerNumber] = value match
    case s"p$numberString" =>
      for
        number <- numberString.toIntOption.toRight(ProtocolError.InvalidInput(numberString, "Expected an int"))
        result <- PlayerNumber.either(number).toInvalidInput(value)
      yield
        result

    case value => Left(ProtocolError.InvalidInput(value, "Invalid player number"))

opaque type Rating = Int :| Positive
object Rating extends RefinedTypeOpsImpl[Int, Positive, Rating]

opaque type TurnNumber = Int :| Positive
object TurnNumber extends RefinedTypeOpsImpl[Int, Positive, TurnNumber]

opaque type PokemonSlot = Int :| Interval.Closed[0, 2]
object PokemonSlot extends RefinedTypeOpsImpl[Int, Interval.Closed[0, 2], PokemonSlot]:

  def fromCode(value: Char): Either[ProtocolError, PokemonSlot] = value match
    case 'a' => Right(0)
    case 'b' => Right(1)
    case 'c' => Right(2)
    case _   => Left(ProtocolError.InvalidInput(value.toString, "Invalid slot"))

opaque type Health = Int :| Positive
object Health extends RefinedTypeOpsImpl[Int, Positive, Health]:

  def fromString(value: String): Either[ProtocolError, Health] = value match
    case s"$health/100" =>
      for
        healthInt <- health.toIntOption.toRight(ProtocolError.InvalidInput(health, "Invalid int"))
        validHealth <- Health.either(healthInt).toInvalidInput(health)
      yield
        validHealth

    case health =>
      for
        healthInt <- health.toIntOption.toRight(ProtocolError.InvalidInput(health, "Invalid int"))
        validHealth <- Health.either(healthInt).toInvalidInput(health)
      yield
        validHealth

opaque type Status = String :| Not[Blank]
object Status extends RefinedTypeOpsImpl[String, Not[Blank], Status]:

  val Burn: Status = "brn"
  val Freeze: Status = "frz"
  val Paralysis: Status = "par"
  val Poison: Status = "psn"
  val Sleep: Status = "slp"