package io.github.projectpidove.showdown.protocol.server.tournament

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

opaque type Loss = Int :| Positive
object Loss extends RefinedTypeOpsImpl[Int, Positive, Loss]

opaque type Encounter = Int :| Interval.Closed[1, 2]
object Encounter extends RefinedTypeOpsImpl[Int, Interval.Closed[1, 2], Encounter]

opaque type Score = Int :| GreaterEqual[0]
object Score extends RefinedTypeOpsImpl[Int, GreaterEqual[0], Score]