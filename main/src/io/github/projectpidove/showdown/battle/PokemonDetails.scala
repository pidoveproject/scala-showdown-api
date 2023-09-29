package io.github.projectpidove.showdown.battle

import scala.util.boundary
import scala.util.boundary.break
import io.github.projectpidove.showdown.protocol.{MessageDecoder, ProtocolError}
import io.github.projectpidove.showdown.protocol.MessageDecoder.toInvalidInput
import io.github.projectpidove.showdown.team.{Gender, Level, SpeciesName, Type}
import io.github.projectpidove.showdown.util.either.*
import zio.json.JsonDecoder

case class PokemonDetails(
  species: SpeciesName,
  shiny: Boolean = false,
  level: Option[Level] = None,
  gender: Option[Gender] = None,
  teraType: Option[Type] = None
):
  
  def merge(details: PokemonDetails): PokemonDetails =
    PokemonDetails(
      details.species,
      details.shiny,
      level = details.level.orElse(level),
      gender = details.gender.orElse(gender),
      teraType = details.teraType.orElse(teraType)
    )

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
    