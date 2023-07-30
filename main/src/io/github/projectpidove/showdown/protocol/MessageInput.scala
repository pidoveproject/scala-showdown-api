package io.github.projectpidove.showdown.protocol

import scala.collection.mutable.ListBuffer

case class ProtocolInput(raw: String, data: List[(Int, String)], cursor: Int):
  
  def exhausted: Boolean = cursor >= data.size

  private def currentEntry: Either[ProtocolError, (Int, String)] =
    if exhausted then Left(ProtocolError.InputExhausted(raw, data.size))
    else Right(data(cursor))

  def characterPosition: Either[ProtocolError, Int] = currentEntry.map(_._1)

  def peek: Either[ProtocolError, String] = currentEntry.map(_._2)

  def skip: ProtocolInput = this.copy(cursor = cursor + 1)

object ProtocolInput:

  def fromInput(input: String): ProtocolInput =
    val data = ListBuffer.empty[(Int, String)]
    var builder = StringBuilder()
    var begin = 0

    for cursor <- 0 to input.length do
      if cursor == input.length || input(cursor) == '|' then
        data += ((begin, builder.toString))
        builder = StringBuilder()
        begin = math.max(cursor+1, input.length)
      else
        builder += input(cursor)

    ProtocolInput(input, data.toList, 0)

  def fromList(list: List[String]): ProtocolInput =
    var cursor = 0
    val data =
      for
        element <- list
      yield
        val elementCursor = cursor
        cursor += element.length
        (elementCursor, element)

    ProtocolInput(list.mkString, data, 0)
