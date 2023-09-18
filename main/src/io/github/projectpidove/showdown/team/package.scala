package io.github.projectpidove.showdown.team

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

opaque type Surname = String :| Not[Blank]
object Surname extends RefinedTypeOpsImpl[String, Not[Blank], Surname]

opaque type SpeciesName = String :| Not[Blank]
object SpeciesName extends RefinedTypeOpsImpl[String, Not[Blank], SpeciesName]

opaque type AbilityName = String :| Not[Blank]
object AbilityName extends RefinedTypeOpsImpl[String, Not[Blank], AbilityName]

opaque type MoveName = String :| Not[Blank]
object MoveName extends RefinedTypeOpsImpl[String, Not[Blank], MoveName]

opaque type ItemName = String :| Not[Blank]
object ItemName extends RefinedTypeOpsImpl[String, Not[Blank], ItemName]

opaque type Level = Int :| Interval.Closed[0, 100]
object Level extends RefinedTypeOpsImpl[Int, Interval.Closed[0, 100], Level]

opaque type DynamaxLevel = Int :| Interval.Closed[0, 10]
object DynamaxLevel extends RefinedTypeOpsImpl[Int, Interval.Closed[0, 10], DynamaxLevel]

opaque type Happiness = Int :| Interval.Closed[0, 255]
object Happiness extends RefinedTypeOpsImpl[Int, Interval.Closed[0, 255], Happiness]

opaque type IV = Int :| Interval.Closed[0, 31]
object IV extends RefinedTypeOpsImpl[Int, Interval.Closed[0, 31], IV]

opaque type EV = Int :| Interval.Closed[0, 255]
object EV extends RefinedTypeOpsImpl[Int, Interval.Closed[0, 255], EV]

opaque type Tier = String :| Not[Blank]
object Tier extends RefinedTypeOpsImpl[String, Not[Blank], Tier]

opaque type TeamName = String :| Not[Blank]
object TeamName extends RefinedTypeOpsImpl[String, Not[Blank], TeamName]

type IVS = Map[StatType, IV]
type EVS = Map[StatType, EV]
type MoveNames = List[MoveName] :| MaxLength[4]
type PokemonSets = List[PokemonSet] :| MaxLength[6]
