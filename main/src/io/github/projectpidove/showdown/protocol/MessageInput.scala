package io.github.projectpidove.showdown.protocol

import scala.collection.mutable.ListBuffer

case class MessageInput(raw: String, data: List[(Int, String)], cursor: Int):
  
  def exhausted: Boolean = cursor >= data.size

  private def currentEntry: Either[ProtocolError, (Int, String)] =
    if exhausted then Left(ProtocolError.InputExhausted(raw, data.size))
    else Right(data(cursor))

  def characterPosition: Either[ProtocolError, Int] = currentEntry.map(_._1)

  def peek: Either[ProtocolError, String] = currentEntry.map(_._2)

  def skip: MessageInput = this.copy(cursor = cursor + 1)

object MessageInput:

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
        begin = math.max(cursor+1, str.length)
      else
        builder += str(cursor)

    MessageInput(str, data.toList, 0)

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
