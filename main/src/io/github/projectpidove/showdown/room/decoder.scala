package io.github.projectpidove.showdown.room

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.projectpidove.showdown.protocol.{MessageDecoder, MessageInput, ProtocolError}
import io.github.projectpidove.showdown.room.ChatContent
import io.github.projectpidove.showdown.user.User

import scala.collection.mutable.ListBuffer
import scala.util.boundary
import scala.util.boundary.break

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
