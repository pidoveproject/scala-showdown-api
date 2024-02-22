package io.github.pidoveproject.showdown.protocol.client

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.pidoveproject.showdown.protocol.MessageEncoder
import io.github.pidoveproject.showdown.protocol.client.DataCommand.EVBoost
import io.github.pidoveproject.showdown.protocol.client.DataCommand.EVBoost.Neutral
import io.github.pidoveproject.showdown.team.{AbilityName, EV, IV, ItemName, Level, MoveName, Nature, SpeciesName, Type}
import io.github.pidoveproject.showdown.{FormatName, Generation}
import io.github.pidoveproject.showdown.util.given

/**
 * A command related to Showdown data, e.g pokedex.
 */
enum DataCommand derives MessageEncoder:

  /**
   * Get the data of a pokedex entry.
   *
   * @param data either a species, an item, a move, an ability or a nature
   */
  case Data(data: SpeciesName | ItemName | MoveName | AbilityName | Nature)

  /**
   * Search in the pokedex.
   *
   * @param query the search query
   */
  case DexSearch(query: String)

  /**
   * Search for a move.
   *
   * @param query the search query
   */
  case MoveSearch(query: String)

  /**
   * Search for an item.
   *
   * @param query the search query
   */
  case ItemSearch(query: String)

  /**
   * Check how a given pokemon can learn a move.
   *
   * @param ruleset the criteria to respect
   * @param pokemon the move learner
   * @param moves the list of moves to check
   */
  case Learn(ruleset: Option[Generation | FormatName], pokemon: SpeciesName, moves: List[MoveName])

  /**
   * Calculate a statistic.
   *
   * @param level the level of the pokemon
   * @param baseStat the base statistic of the pokemon
   * @param iv the iv of the pokemon
   * @param ev the investment and nature of the pokemon in the statistic to calculate
   * @param modifier a statistic modifier, like the effect of an item
   */
  case StatCalc(level: Option[Level], baseStat: Int :| Positive, iv: Option[IV], ev: Option[EVBoost], modifier: Option[Modifier])

  /**
   * Get the effectiveness of a move or type against a pokemon or another type.
   *
   * @param attacker the attacking type or move
   * @param defender the defending type or species
   */
  case Effectiveness(attacker: MoveName | Type, defender: SpeciesName | Type)

  /**
   * Get the weaknesses of a/two type(s) or a pokemon.
   *
   * @param typesOrPokemon a type, double-type or a pokemon
   */
  case Weakness(typesOrPokemon: SpeciesName | (Type, Type) | Type)

  /**
   * Check the type coverage of a list of moves.
   *
   * @param moves the moves to test
   */
  case Coverage(moves: List[MoveName])

  /**
   * Search for a random move.
   *
   * @param criteria optional search query
   */
  case RandomMove(criteria: Option[String])

  /**
   * Search for a random pokemon.
   *
   * @param cirteria optional search query
   */
  case RandomPokemon(criteria: Option[String])

object DataCommand:

  /**
   * EV + Nature boost
   */
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

