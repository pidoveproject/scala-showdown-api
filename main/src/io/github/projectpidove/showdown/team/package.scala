package io.github.projectpidove.showdown.team

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

type Surname = String :| Not[Blank]
object Surname extends RefinedTypeOpsImpl[String, Not[Blank], Surname]

type SpeciesName = String :| Not[Blank]
object SpeciesName extends RefinedTypeOpsImpl[String, Not[Blank], SpeciesName]

type AbilityName = String :| Not[Blank]
object AbilityName extends RefinedTypeOpsImpl[String, Not[Blank], AbilityName]

type MoveName = String :| Not[Blank]
object MoveName extends RefinedTypeOpsImpl[String, Not[Blank], MoveName]

type ItemName = String :| Not[Blank]
object ItemName extends RefinedTypeOpsImpl[String, Not[Blank], ItemName]

type Level = Int :| Interval.Closed[0, 100]
object Level extends RefinedTypeOpsImpl[Int, Interval.Closed[0, 100], Level]

type DynamaxLevel = Int :| Interval.Closed[0, 10]
object DynamaxLevel extends RefinedTypeOpsImpl[Int, Interval.Closed[0, 10], DynamaxLevel]

type Happiness = Int :| Interval.Closed[0, 255]
object Happiness extends RefinedTypeOpsImpl[Int, Interval.Closed[0, 255], Happiness]

type IV = Int :| Interval.Closed[0, 31]
object IV extends RefinedTypeOpsImpl[Int, Interval.Closed[0, 31], IV]

type EV = Int :| Interval.Closed[0, 255]
object EV extends RefinedTypeOpsImpl[Int, Interval.Closed[0, 255], EV]

type Tier = String :| Pure
object Tier extends RefinedTypeOpsImpl[String, Pure, Tier]

type IVS = Map[StatType, IV]
type EVS = Map[StatType, EV]
type MoveNames = List[MoveName] :| MaxLength[4]
type PokemonSets = List[PokemonSet] :| MaxLength[6]
