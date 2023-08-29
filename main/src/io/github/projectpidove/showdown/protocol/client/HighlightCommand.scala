package io.github.projectpidove.showdown.protocol.client

import io.github.projectpidove.showdown.protocol.MessageEncoder
import scala.collection.immutable

/**
 * A command bound to the highlighting feature.
 */
enum HighlightCommand:

  /**
   * Add words to the highlighting list.
   *
   * @param words the list of words to highlight
   */
  case Add(words: immutable.List[String])

  /**
   * Add words to a room-specific highlighting list.
   *
   * @param words the list of words to highlight
   */
  case RoomAdd(words: immutable.List[String])

  case List
  case RoomList

  /**
   * Remove words from the highlighting list.
   *
   * @param words the list of words to not highlight
   */
  case Delete(words: immutable.List[String])

  /**
   * Remove words from a room-specific highlighting list.
   *
   * @param words the list of words to not highlight
   */
  case RoomDelete(words: immutable.List[String])

  /**
   * Clear the global highlighting list.
   */
  case Clear

  /**
   * Clear the highlighting list of the current room.
   */
  case RoomClear

  /**
   * Clear both global and room highlighting list.
   */
  case ClearAll

object HighlightCommand:

  given MessageEncoder[HighlightCommand] =
    MessageEncoder.string.zip(MessageEncoder.derived[HighlightCommand]).contramap(value => ("highlight", value))
