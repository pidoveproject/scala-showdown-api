package io.github.projectpidove.showdown

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

opaque type Timestamp = Long :| Positive
object Timestamp extends RefinedTypeOpsImpl[Long, Positive, Timestamp]

opaque type Count = Int :| Positive
object Count extends RefinedTypeOpsImpl[Int, Positive, Count]

opaque type ChallStr = String :| FixedLength[258]
object ChallStr extends RefinedTypeOpsImpl[String, FixedLength[258], ChallStr]