package io.github.pidoveproject.showdown

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.pidoveproject.showdown.util.NumericTypeOps

opaque type Timestamp = Long :| GreaterEqual[0]
object Timestamp extends RefinedTypeOps[Long, GreaterEqual[0], Timestamp]:

  def zero: Timestamp = 0

opaque type Count = Int :| GreaterEqual[0]
object Count extends NumericTypeOps[Int, GreaterEqual[0], Count]

opaque type ChallStr = String :| FixedLength[258]
object ChallStr extends RefinedTypeOps[String, FixedLength[258], ChallStr]

opaque type FormatName = String :| Not[Blank]
object FormatName extends RefinedTypeOps[String, Not[Blank], FormatName]:

  def unapply(value: String): Option[FormatName] = this.option(value)

opaque type FormatCategoryName = String :| Not[Blank]
object FormatCategoryName extends RefinedTypeOps[String, Not[Blank], FormatCategoryName]

opaque type Generation = Int :| Positive
object Generation extends RefinedTypeOps[Int, Positive, Generation]
