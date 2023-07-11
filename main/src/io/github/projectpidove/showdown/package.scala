package io.github.projectpidove.showdown

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

opaque type Level = Int :| Interval.Closed[0, 100]
object Level extends RefinedTypeOps[Level]

opaque type DynamaxLevel = Int :| Interval.Closed[0, 10]
object DynamaxLevel extends RefinedTypeOps[DynamaxLevel]

opaque type Happiness = Int :| Interval.Closed[0, 255]
object Happiness extends RefinedTypeOps[Happiness]

opaque type IV = Int :| Interval.Closed[0, 31]
object IV extends RefinedTypeOps[IV]

opaque type EV = Int :| Interval.Closed[0, 255]

object EV extends RefinedTypeOps[EV]

type IVS = Map[StatType, IV]
type EVS = Map[StatType, EV]
type MoveNames = List[String] :| MaxLength[4]
