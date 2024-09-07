package io.github.pidoveproject.showdown.protocol.server.tournament

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

opaque type Loss = Int :| Positive
object Loss extends RefinedTypeOps[Int, Positive, Loss]

opaque type Encounter = Int :| Interval.Closed[1, 2]
object Encounter extends RefinedTypeOps[Int, Interval.Closed[1, 2], Encounter]

opaque type Score = Int :| GreaterEqual[0]
object Score extends RefinedTypeOps[Int, GreaterEqual[0], Score]
