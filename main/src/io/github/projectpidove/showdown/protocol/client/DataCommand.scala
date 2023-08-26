package io.github.projectpidove.showdown.protocol.client

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.projectpidove.showdown.protocol.MessageEncoder
import io.github.projectpidove.showdown.protocol.client.DataCommand.EVBoost
import io.github.projectpidove.showdown.protocol.client.DataCommand.EVBoost.Neutral
import io.github.projectpidove.showdown.team.{AbilityName, EV, IV, ItemName, Level, MoveName, Nature, SpeciesName, Type}
import io.github.projectpidove.showdown.{FormatName, Generation}
import io.github.projectpidove.showdown.util.given

enum DataCommand derives MessageEncoder:
  case Data(data: SpeciesName | ItemName | MoveName | AbilityName | Nature)
  case DexSearch(query: String)
  case MoveSearch(query: String)
  case ItemSearch(query: String)
  case Learn(ruleset: Option[Generation | FormatName], pokemon: SpeciesName, moves: List[MoveName])
  case StatCalc(level: Option[Level], baseStat: Int :| Positive, iv: Option[IV], ev: Option[EVBoost], modifier: Option[Modifier])
  case Effectiveness(attacker: MoveName | Type, defender: SpeciesName | Type)
  case Weakness(typesOrPokemon: SpeciesName | (Type, Type))
  case Coverage(moves: List[MoveName])
  case RandomMove(criteria: Option[String])
  case RandomPokemon(criteria: Option[String])

object DataCommand:

  enum EVBoost:
    case Neutral(value: EV = EV(252))
    case Buffed(value: EV = EV(252))
    case Nerfed(value: EV = EV(252))

  given MessageEncoder[EVBoost] =
    case EVBoost.Neutral(value) => Right(List(s"${value}ev"))
    case EVBoost.Buffed(value) => Right(List(s"${value}ev+"))
    case EVBoost.Nerfed(value) => Right(List(s"${value}ev-"))

  given MessageEncoder[Generation] =
    MessageEncoder.string.contramap(gen => s"gen$gen")

  given MessageEncoder[Level] =
    MessageEncoder.string.contramap(level => s"lv$level")

  given MessageEncoder[IV] =
    MessageEncoder.string.contramap(iv => s"${iv}iv")

