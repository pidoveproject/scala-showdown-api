package io.github.pidoveproject.showdown.battle

import scala.util.boundary
import scala.util.boundary.break
import io.github.pidoveproject.showdown.protocol.{MessageDecoder, ProtocolError}
import io.github.pidoveproject.showdown.protocol.MessageDecoder.toInvalidInput
import io.github.pidoveproject.showdown.team.{Gender, Level, SpeciesName, Type}
import io.github.pidoveproject.showdown.util.either.*
import zio.json.JsonDecoder

/**
 * The known permanent details of a pokemon.
 *
 * @param species the species of the pokemon
 * @param shiny whether the pokemon is shiny or not
 * @param level the level of the pokemon
 * @param gender the gender of the pokemon
 * @param teraType the tera type of the pokememon
 */
case class PokemonDetails(
  species: SpeciesName,
  shiny: Boolean = false,
  level: Option[Level] = None,
  gender: Option[Gender] = None,
  teraType: Option[Type] = None
):

  /**
   * Merge these details with others. Used when new details are discovered.
   *
   * @param details the details update
   * @return a copy of these details overridden with `details`
   */
  def merge(details: PokemonDetails): PokemonDetails =
    PokemonDetails(
      details.species,
      details.shiny,
      level = details.level.orElse(level),
      gender = details.gender.orElse(gender),
      teraType = details.teraType.orElse(teraType)
    )

  private def isSpeciesCompatibleWith(otherSpecies: SpeciesName): Boolean = (species, otherSpecies) match
    case (s"$baseSpecies-*", s"$otherBaseSpecies-$_") => baseSpecies == otherBaseSpecies
    case _ => species == otherSpecies

  /**
   * Check if these details are compatible with the given ones.
   * 
   * @param details the details to compare
   * @return whether the given details might represent the same pokemon or not
   */
  def isCompatible(details: PokemonDetails): Boolean =
    isSpeciesCompatibleWith(details.species)
      && (!shiny || details.shiny)
      && level == details.level
      && gender == details.gender
      && teraType == details.teraType

  /**
   * Alias for [[isCompatible]]
   */
  def ~=(details: PokemonDetails): Boolean = isCompatible(details)

object PokemonDetails:
  
  private def parse(species: SpeciesName, details: String): Either[ProtocolError, PokemonDetails] =
    val parts = details.split(", ")

    boundary[Either[ProtocolError, PokemonDetails]]:
      val result = parts.foldLeft(PokemonDetails(species)): (state, part) =>
        part match
          case "shiny" => state.copy(shiny = true)
          case s"L$level" =>
            val parsedLevel = level.toIntOption.getOrBreak(ProtocolError.InvalidInput(level, "Level should be an integer"))
            val validLevel = Level.refineOrBreak(parsedLevel)

            state.copy(level = Some(validLevel))
          case "M" => state.copy(gender = Some(Gender.Male))
          case "F" => state.copy(gender = Some(Gender.Female))
          case s"tera:$tpe" => state.copy(teraType = Some(Type.fromName(tpe).getOrBreak(ProtocolError.InvalidInput(tpe, "Invalid type"))))
          case _ => break(Left(ProtocolError.InvalidInput(part, "Invalid detail")))

      Right(result)

  /**
   * Parse the details of a pokemon from a [[String]].
   * 
   * @param value the text to parse
   * @return the parsed details or a [[ProtocolError]] if it failed
   */
  def fromString(value: String): Either[ProtocolError, PokemonDetails] = value match
    case s"$species, $details" =>
      for
        validSpecies <- SpeciesName.either(species).toInvalidInput(species)
        result <- parse(validSpecies, details)
      yield
        result

    case species =>
      SpeciesName.either(species).toInvalidInput(species).map(PokemonDetails(_))

  given MessageDecoder[PokemonDetails] = MessageDecoder.string.mapEither(fromString)
  
  given JsonDecoder[PokemonDetails] = JsonDecoder.string.mapOrFail(fromString(_).left.map(_.getMessage))
    