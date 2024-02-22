package io.github.pidoveproject.showdown.protocol.server.tournament

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.iltotore.iron.zioJson.given
import io.github.pidoveproject.showdown.Count
import io.github.pidoveproject.showdown.protocol.{MessageDecoder, ProtocolError}
import io.github.pidoveproject.showdown.protocol.MessageDecoder.{given, *}
import zio.json.JsonDecoder

/**
 * The generator for a tournament bracket.
 */
enum TournamentGenerator:

  /**
   * Elimination BO tree.
   *
   * @param maxLoss the max number of loss before loosing a BO
   */
  case Elimination(maxLoss: Count)
  case RoundRobin(maxEncounter: Count)

object TournamentGenerator:

  private def wordToCount(word: String): Either[String, Count] = word match
    case "Single" => Right(Count(1))
    case "Double" => Right(Count(2))
    case "Triple" => Right(Count(3))
    case _        => Left("Invalid count name")

  given JsonDecoder[TournamentGenerator] =
    JsonDecoder
      .string
      .mapOrFail: raw =>
        val separated = raw.split(" ")

        separated match
          case Array("Round", "Robin")         => Right(RoundRobin(Count(1)))
          case Array(prefix, "Round", "Robin") => wordToCount(prefix).map(RoundRobin.apply)
          case Array(prefix, "Elimination")    => wordToCount(prefix).map(Elimination.apply)
          case _                               => Left("Invalid tournament generator")

  given MessageDecoder[TournamentGenerator] =
    MessageDecoder
      .string
      .mapEither: raw =>
        val separated = raw.split(" ")

        separated match
          case Array("Round", "Robin")         => Right(RoundRobin(Count(1)))
          case Array(prefix, "Round", "Robin") => wordToCount(prefix).map(RoundRobin.apply).toInvalidInput(prefix)
          case Array(prefix, "Elimination")    => wordToCount(prefix).map(Elimination.apply).toInvalidInput(prefix)
          case _                               => Left(ProtocolError.InvalidInput(raw, "Invalid tournament generator"))
