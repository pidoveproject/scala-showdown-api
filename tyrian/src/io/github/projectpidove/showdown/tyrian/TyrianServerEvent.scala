package io.github.projectpidove.showdown.tyrian

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.*
import io.github.projectpidove.showdown.protocol.server.ServerMessage
import io.github.projectpidove.showdown.protocol.{MessageDecoder, MessageInput, ProtocolError}
import io.github.projectpidove.showdown.room.RoomId
import tyrian.websocket.WebSocketEvent

enum TyrianServerEvent:
  case Open
  case Receive(messages: List[Either[ProtocolError, ServerMessage]])
  case Error(error: ProtocolError)
  case Close(code: Int, reason: String)
  case Heartbeat

object TyrianServerEvent:

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