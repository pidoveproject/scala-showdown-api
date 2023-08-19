package io.github.projectpidove.showdown.protocol

import scala.collection.mutable.ListBuffer

/**
 * A message input.
 *
 * @param raw the entire input as String
 * @param data the input separated in indexed parts
 * @param cursor the next part index to consume
 */
case class MessageInput(raw: String, data: List[(Int, String)], cursor: Int):

  /**
   * Check if this input is exhausted.
   *
   * @return true if the cursor is at the end of the input
   */
  def exhausted: Boolean = cursor >= data.size

  private def currentEntry: Either[ProtocolError, (Int, String)] =
    if exhausted then Left(ProtocolError.InputExhausted(raw, data.size))
    else Right(data(cursor))

  /**
   * Get the character position of the cursor.
   *
   * @return the column position of the current part to read
   */
  def characterPosition: Either[ProtocolError, Int] = currentEntry.map(_._1)

  /**
   * Get the next part.
   *
   * @return the part at the cursor's index
   */
  def peek: Either[ProtocolError, String] = currentEntry.map(_._2)

  /**
   * Jump to the next part.
   *
   * @return a new MessageInput pointing to the next part
   */
  def skip: MessageInput = this.copy(cursor = cursor + 1)

object MessageInput:

  /**
   * Create a MessageInput from String.
   *
   * @param input the raw textual input
   * @return a MessageInput resulting from the parsed raw String
   */
  def fromInput(input: String): MessageInput =
    val str =
      if input.startsWith("|") then input.tail
      else input

    val data = ListBuffer.empty[(Int, String)]
    var builder = StringBuilder()
    var begin = 0

    for cursor <- 0 to str.length do
      if cursor == str.length || str(cursor) == '|' then
        data += ((begin, builder.toString))
        builder = StringBuilder()
        begin = math.max(cursor + 1, str.length)
      else
        builder += str(cursor)

    MessageInput(str, data.toList, 0)

  /**
   * Create a MessageInput from the a list of parts.
   *
   * @param list the list of textual parts of a message
   * @return a new MessageInput with the given parts, indexed
   */
  def fromList(list: List[String]): MessageInput =
    var cursor = 0
    val data =
      for
        element <- list
      yield
        val elementCursor = cursor
        cursor += element.length
        (elementCursor, element)

    MessageInput(list.mkString, data, 0)
