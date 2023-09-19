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

opaque type TeamSlot = Int :| Interval.Closed[1, 6]
object TeamSlot extends RefinedTypeOpsImpl[Int, Interval.Closed[1, 6], TeamSlot]

opaque type MoveSlot = Int :| Interval.Closed[1, 4]
object MoveSlot extends RefinedTypeOpsImpl[Int, Interval.Closed[1, 4], MoveSlot]

opaque type StatusEffect = String :| Not[Blank]
object StatusEffect extends RefinedTypeOpsImpl[String, Not[Blank], StatusEffect]:

  val Burn: StatusEffect = "brn"
  val Freeze: StatusEffect = "frz"
  val Paralysis: StatusEffect = "par"
  val Poison: StatusEffect = "psn"
  val Sleep: StatusEffect = "slp"

opaque type Effect = String :| Not[Blank]
object Effect extends RefinedTypeOpsImpl[String, Not[Blank], Effect]

opaque type StatBoost = Int :| Interval.Closed[-6, 6]
object StatBoost extends RefinedTypeOpsImpl[Int, Interval.Closed[-6, 6], StatBoost]

opaque type Weather = String :| Not[Blank]
object Weather extends RefinedTypeOpsImpl[String, Not[Blank], Weather]:

  val DesolateLand: Weather = "desolate land"
  val Hail: Weather = "hail"
  val PrimordialSea: Weather = "primordial sea"
  val Rain: Weather = "rain"
  val Sandstorm: Weather = "sandstorm"
  val Snow: Weather = "snow"
  val Sun: Weather = "sun"
  
opaque type FieldEffect = String :| Not[Blank]
object FieldEffect extends RefinedTypeOpsImpl[String, Not[Blank], FieldEffect]

opaque type SideFieldEffect = String :| Not[Blank]
object SideFieldEffect extends RefinedTypeOpsImpl[String, Not[Blank], SideFieldEffect]

opaque type VolatileStatus = String :| Not[Blank]
object VolatileStatus extends RefinedTypeOpsImpl[String, Not[Blank], VolatileStatus]

opaque type PP = Int :| Positive
object PP extends RefinedTypeOpsImpl[Int, Positive, PP]