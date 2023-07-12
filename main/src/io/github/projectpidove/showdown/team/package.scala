package io.github.projectpidove.showdown.team

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

opaque type Surname <: String = String :| Not[Blank]
object Surname extends RefinedTypeOps[Surname]

opaque type SpeciesName <: String = String :| Not[Blank]
object SpeciesName extends RefinedTypeOps[SpeciesName]

opaque type AbilityName <: String = String :| Not[Blank]
object AbilityName extends RefinedTypeOps[AbilityName]

opaque type MoveName <: String = String :| Not[Blank]
object MoveName extends RefinedTypeOps[MoveName]

opaque type ItemName <: String = String :| Not[Blank]
object ItemName extends RefinedTypeOps[ItemName]

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
type MoveNames = List[MoveName] :| MaxLength[4]
