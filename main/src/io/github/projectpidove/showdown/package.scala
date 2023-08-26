package io.github.projectpidove.showdown

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

opaque type Timestamp = Long :| Positive
object Timestamp extends RefinedTypeOpsImpl[Long, Positive, Timestamp]

opaque type Count = Int :| Positive
object Count extends RefinedTypeOpsImpl[Int, Positive, Count]

opaque type ChallStr = String :| FixedLength[258]
object ChallStr extends RefinedTypeOpsImpl[String, FixedLength[258], ChallStr]

opaque type FormatName = String :| Not[Blank]
object FormatName extends RefinedTypeOpsImpl[String, Not[Blank], FormatName]

opaque type FormatCategoryName = String :| Not[Blank]
object FormatCategoryName extends RefinedTypeOpsImpl[String, Not[Blank], FormatCategoryName]

opaque type Generation = Int :| Positive
object Generation extends RefinedTypeOpsImpl[Int, Positive, Generation]