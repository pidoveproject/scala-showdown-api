package io.github.projectpidove.showdown

import io.github.projectpidove.showdown.protocol.{MessageDecoder, ProtocolError}
import MessageDecoder.*

/**
 * A format category.
 *
 * @param name this category's name
 * @param column the displayed column of this category (used in Showdown's format selection menu)
 * @param formats the formats of this category
 */
case class FormatCategory(name: FormatCategoryName, column: Int, formats: List[Format])

object FormatCategory:

  given (using formatDecoder: MessageDecoder[Format]): MessageDecoder[FormatCategory] =
    for
      columnStr <- MessageDecoder.string
      column <- MessageDecoder.attemptOrElse(columnStr.split(",")(1).toInt, _ => ProtocolError.InvalidInput(columnStr, "Invalid format column"))
      name <- MessageDecoder.string.mapEither(input => FormatCategoryName.either(input).toInvalidInput(input))
      formats <- formatDecoder.repeatUntilCurrent(_.split(",").head.isEmpty)
    yield FormatCategory(name, column, formats)
