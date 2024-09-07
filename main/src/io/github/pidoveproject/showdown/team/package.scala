package io.github.pidoveproject.showdown.team

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

opaque type Surname = String :| Not[Blank]
object Surname extends RefinedTypeOps[String, Not[Blank], Surname]

opaque type SpeciesName = String :| Not[Blank]
object SpeciesName extends RefinedTypeOps[String, Not[Blank], SpeciesName]

opaque type AbilityName = String :| Not[Blank]
object AbilityName extends RefinedTypeOps[String, Not[Blank], AbilityName]

opaque type MoveName = String :| Not[Blank]
object MoveName extends RefinedTypeOps[String, Not[Blank], MoveName]

opaque type ItemName = String :| Not[Blank]
object ItemName extends RefinedTypeOps[String, Not[Blank], ItemName]

opaque type Level = Int :| Interval.Closed[0, 100]
object Level extends RefinedTypeOps[Int, Interval.Closed[0, 100], Level]

opaque type DynamaxLevel = Int :| Interval.Closed[0, 10]
object DynamaxLevel extends RefinedTypeOps[Int, Interval.Closed[0, 10], DynamaxLevel]

opaque type Happiness = Int :| Interval.Closed[0, 255]
object Happiness extends RefinedTypeOps[Int, Interval.Closed[0, 255], Happiness]

opaque type IV = Int :| Interval.Closed[0, 31]
object IV extends RefinedTypeOps[Int, Interval.Closed[0, 31], IV]

opaque type EV = Int :| Interval.Closed[0, 255]
object EV extends RefinedTypeOps[Int, Interval.Closed[0, 255], EV]

opaque type Tier = String :| Not[Blank]
object Tier extends RefinedTypeOps[String, Not[Blank], Tier]

opaque type TeamName = String :| Not[Blank]
object TeamName extends RefinedTypeOps[String, Not[Blank], TeamName]

opaque type Stat = Int :| Positive
object Stat extends RefinedTypeOps[Int, Positive, Stat]

type IVS = Map[StatType, IV]
type EVS = Map[StatType, EV]
type MoveNames = List[MoveName] :| MaxLength[4]
type PokemonSets = List[PokemonSet] :| MaxLength[6]
