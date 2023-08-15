package io.github.projectpidove.showdown

import io.github.projectpidove.showdown.protocol.{MessageDecoder, ProtocolError}
import MessageDecoder.*

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