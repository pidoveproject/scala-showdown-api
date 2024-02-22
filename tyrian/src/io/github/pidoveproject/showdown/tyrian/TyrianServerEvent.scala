package io.github.pidoveproject.showdown.tyrian

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.*
import io.github.pidoveproject.showdown.protocol.server.ServerMessage
import io.github.pidoveproject.showdown.protocol.{MessageDecoder, MessageInput, ProtocolError}
import io.github.pidoveproject.showdown.room.RoomId
import tyrian.websocket.WebSocketEvent

/**
 * A Tyrian event related to the Showdown's server messages.
 */
enum TyrianServerEvent:

  /**
   * The connection is now open. Usually sent first.
   */
  case Open

  /**
   * One or more [[ServerMessage]]s have been received.
   *
   * @param messages the list of decoded messages and decoding failure in reception order
   */
  case Receive(messages: List[Either[ProtocolError, ServerMessage]])

  /**
   * An error occurred.
   */
  case Error(error: ProtocolError)

  /**
   * The is closed by the server.
   *
   * @param code the closing code of this web socket
   * @param reason the cause of the connection closing
   */
  case Close(code: Int, reason: String)

  /**
   * Keep-alive message.
   */
  case Heartbeat

object TyrianServerEvent:

  /**
   * Create a [[TyrianServerEvent]] from a web socket message.
   * 
   * @param event the received socket message
   * @param decoder the decoder of a message
   * @return a new server-related event created from the received socket message
   */
  def fromTyrian(event: WebSocketEvent)(using decoder: MessageDecoder[ServerMessage]): TyrianServerEvent = event match
    case WebSocketEvent.Open => Open
    case WebSocketEvent.Receive(text) =>
      val result =
        for
          messagesAndRoom <- text.split(raw"(\r\n|\r|\n)").toList match
            case s">$room" :: tail => RoomId.either(room).map((tail, _))
            case messages => Right((messages, RoomId("lobby")))
        yield
          val messages = messagesAndRoom._1
          val room = messagesAndRoom._2
          messages.map(msg => decoder.decode(MessageInput.fromInput(msg, room)))

      result.fold(err => Error(ProtocolError.InvalidInput(text, err)), Receive.apply)

    case WebSocketEvent.Error(error) => Error(ProtocolError.Miscellaneous(error))
    case WebSocketEvent.Close(code, reason) => Close(code, reason)
    case WebSocketEvent.Heartbeat => Heartbeat