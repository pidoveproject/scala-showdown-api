package io.github.pidoveproject.showdown.tyrian

import cats.effect.Async
import cats.syntax.all.*
import io.github.iltotore.iron.*
import io.github.pidoveproject.showdown.protocol.{MessageInput, ProtocolError}
import io.github.pidoveproject.showdown.protocol.client.ClientMessage
import io.github.pidoveproject.showdown.protocol.server.ServerMessage
import io.github.pidoveproject.showdown.room.RoomId
import io.github.pidoveproject.showdown.ShowdownConnection
import tyrian.{Cmd, Sub}
import tyrian.websocket.{WebSocket, WebSocketEvent}

/**
 * An open connection to a Pokemon Showdown.
 *
 * @param socket the web socket used to communicate with the server
 * @tparam F the effect type of the Tyrian app
 */
case class TyrianShowdownConnection[F[_]: Async](socket: WebSocket[F])
    extends ShowdownConnection[String, [e, r] =>> Cmd[F, UnitToNothing[r]], [r] =>> Sub[F, TyrianConnectionEvent[r]]]:

  override def sendRawMessage(message: String): Cmd[F, Nothing] =
    socket.publish(message)

  override def sendMessage(room: RoomId, message: ClientMessage): Cmd[F, Nothing] =
    ClientMessage.encoder
      .encode(message)
      .fold(
        err => Cmd.SideEffect(Async[F].raiseError(err)),
        msg => sendRawMessage(msg.mkString(s"$room|/", ",", "").replaceFirst(",", " "))
      )

  override def sendMessage(message: ClientMessage): Cmd[F, Nothing] =
    ClientMessage.encoder
      .encode(message)
      .fold(
        err => Cmd.SideEffect(Async[F].raiseError(err)),
        msg => sendRawMessage(msg.mkString(s"|/", ",", "").replaceFirst(",", " "))
      )

  override def disconnect(): Cmd[F, Nothing] = socket.disconnect

  override val serverMessages: Sub[F, TyrianConnectionEvent[Either[ProtocolError, ServerMessage]]] =
    socket.subscribe:
      case WebSocketEvent.Receive(text) =>
        text
          .split(raw"(\r\n|\r|\n)")
          .toList
          .match
            case s">$room" :: tail => RoomId.either(room).map((tail, _))
            case messages          => Right((messages, RoomId("lobby")))
          .match
            case Right((messages, room)) => TyrianConnectionEvent.Receive(
                messages.map(message =>
                  ServerMessage
                    .decoder
                    .decodeZPure(MessageInput.fromInput(message, room))
                    .runEither
                )
              )

            case Left(error) => TyrianConnectionEvent.Receive(List(Left(ProtocolError.InvalidInput(text, error))))

      case WebSocketEvent.Open                => TyrianConnectionEvent.Open
      case WebSocketEvent.Close(code, reason) => TyrianConnectionEvent.Close(code, reason)
      case WebSocketEvent.Error(error)        => TyrianConnectionEvent.Error(error)
      case WebSocketEvent.Heartbeat           => TyrianConnectionEvent.Heartbeat
