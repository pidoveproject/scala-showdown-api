package io.github.projectpidove.showdown

import io.github.projectpidove.showdown.protocol.{MessageDecoder, ProtocolError}

case class FormatCategory(name: String, column: Int, formats: List[Format])

object FormatCategory:

  given (using formatDecoder: MessageDecoder[Format]): MessageDecoder[FormatCategory] =
    for
      columnStr <- MessageDecoder.string
      column <- MessageDecoder.attemptOrElse(columnStr.split(",")(1).toInt, _ => ProtocolError.InvalidInput(columnStr, "Invalid format column"))
      name <- MessageDecoder.string
      formats <- formatDecoder.repeatUntilCurrent(_.split(",").head.isEmpty)
    yield FormatCategory(name, column, formats)
