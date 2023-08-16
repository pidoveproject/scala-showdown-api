package io.github.projectpidove.showdown.protocol.client

import io.github.projectpidove.showdown.protocol.MessageEncoder
import scala.collection.immutable

enum HighlightCommand:
  case Add(words: immutable.List[String])
  case RoomAdd(words: immutable.List[String])
  case List()
  case RoomList()
  case Delete(words: immutable.List[String])
  case RoomDelete(words: immutable.List[String])
  case Clear()
  case RoomClear()
  case ClearAll()

object HighlightCommand:

  given MessageEncoder[HighlightCommand] =
    MessageEncoder.string.zip(MessageEncoder.derived[HighlightCommand]).contramap(value => ("highlight", value))
