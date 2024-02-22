package io.github.pidoveproject.showdown

import io.github.pidoveproject.showdown.protocol.{MessageDecoder, ProtocolError}
import MessageDecoder.*

/**
 * Represents a battle format e.g. OU 9G format
 *
 * @param name the name of this format e.g `gen9ou`
 * @param random whether this format uses random teams or not
 * @param searchOnly whether this format can only be searched (therefore not played) or not
 * @param challengeOnly whether this format is only available when challenging a user or not
 */
case class Format(name: FormatName, random: Boolean = false, searchOnly: Boolean = false, challengeOnly: Boolean = false)

object Format:

  given MessageDecoder[Format] =
    MessageDecoder
      .string
      .map(_.split(",").toList)
      .mapEither {
        case list @ ("" :: _ | Nil) => Left(ProtocolError.InvalidInput(list.mkString(","), "Invalid format"))
        case name :: tail =>
          var random = false
          var searchOnly = false
          var challengeOnly = false

          var remaining = tail
          while remaining.nonEmpty do
            remaining match
              case "" :: "" :: remainingTail =>
                searchOnly = true
                remaining = remainingTail
              case "" :: tail =>
                challengeOnly = true
                remaining = remaining.tail
              case _ =>
                random = true
                remaining = remaining.tail

          for
            formatName <- FormatName.either(name).toInvalidInput(name)
          yield Format(formatName, random, searchOnly, challengeOnly)
      }
