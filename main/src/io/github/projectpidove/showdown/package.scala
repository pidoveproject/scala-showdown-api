package io.github.projectpidove.showdown

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

opaque type Level = Int :| Interval.Closed[0, 100]

opaque type DynamaxLevel = Int :| Interval.Closed[0, 10]

opaque type Happiness = Int :| Interval.Closed[0, 255]

opaque type IV = Int :| Interval.Closed[0, 31]

opaque type EV = Int :| Interval.Closed[0, 255]

type IVS = Map[StatType, IV]
type EVS = Map[StatType, EV]
type MoveNames = List[String] :| MaxLength[4]
