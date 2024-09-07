package io.github.pidoveproject.showdown.room

import io.github.pidoveproject.showdown.protocol.{MessageDecoder, ProtocolError}
import io.github.pidoveproject.showdown.room.ChatContent

given chatDecoder: MessageDecoder[ChatContent] =
  MessageDecoder.string
    .repeatUntilEnd
    .mapEither(list => ChatContent.either(list.mkString("|")).left.map(x => ProtocolError.InvalidInput(x, "Blank message")))

given popupDecoder: MessageDecoder[PopupMessage] =
  MessageDecoder.string
    .repeatUntilEnd
    .mapEither: list =>

      val withNewlines =
        list.map: str =>
          if str.isEmpty then System.lineSeparator()
          else str

      PopupMessage
        .either(withNewlines.mkString(""))
        .left
        .map(x => ProtocolError.InvalidInput(x, "Blank message"))
