package io.github.pidoveproject.showdown.cats

import cats.{Monad, MonadError}
import cats.effect.IO
import cats.effect.kernel.Concurrent
import cats.syntax.all.*
import fs2.Stream
import io.github.iltotore.iron.*
import io.github.pidoveproject.showdown.{ChallStr, ShowdownConnection}
import io.github.pidoveproject.showdown.protocol.{LoginResponse, MessageInput, ProtocolError}
import io.github.pidoveproject.showdown.protocol.client.{AuthCommand, ClientMessage}
import io.github.pidoveproject.showdown.protocol.server.ServerMessage
import io.github.pidoveproject.showdown.room.RoomId
import io.github.pidoveproject.showdown.user.Username
import org.http4s.{Method, Request, Uri, UrlForm}
import org.http4s.EntityDecoder.text
import org.http4s.client.Client
import org.http4s.client.websocket.{WSConnection, WSFrame}
import zio.json.*

class CatsShowdownConnection[F[_] : Concurrent](client: Client[F], connection: WSConnection[F])
  extends ShowdownConnection[WSFrame, [e, r] =>> F[r], [r] =>> Stream[F, r]]:

  override def sendRawMessage(message: WSFrame): F[Unit] =
    connection.send(message)

  override def sendMessage(room: RoomId, message: ClientMessage): F[Unit] =
    for
      parts <- ClientMessage.encoder.encode(message).pure.rethrow
      command = parts.mkString(s"$room|/", ",", "").replaceFirst(",", " ")
      _ <- sendRawMessage(WSFrame.Text(command))
    yield ()

  override def sendMessage(message: ClientMessage): F[Unit] =
    for
      parts <- ClientMessage.encoder.encode(message).pure.rethrow
      command = parts.mkString(s"|/", ",", "").replaceFirst(",", " ")
      _ <- sendRawMessage(WSFrame.Text(command))
    yield ()

  override def disconnect(): F[Unit] = connection.send(WSFrame.Close(0, "OK"))

  override val serverMessages: Stream[F, Either[ProtocolError, ServerMessage]] =
    connection
      .receiveStream
      .flatMap:
        case WSFrame.Text(text, _) =>
          text
            .split(raw"(\r\n|\r|\n)")
            .toList
            .match
              case s">$room" :: tail => RoomId.either(room).map((tail, _))
              case messages => Right((messages, RoomId("lobby")))
            .match
              case Right((messages, room)) => Stream.emits(messages).map(message =>
                ServerMessage
                  .decoder
                  .decodeZPure(MessageInput.fromInput(message, room))
                  .runEither
              )

              case Left(error) => Stream.emit(Left(ProtocolError.InvalidInput(text, error)))

        case _ => Stream.empty

  override def login(challStr: ChallStr)(name: Username, password: String): F[LoginResponse] =
    client.expect[String](
      Request(
        method = Method.POST,
        uri = Uri.unsafeFromString("https://play.pokemonshowdown.com/action.php")
      ).withEntity(UrlForm(
        "act" -> "login",
        "name" -> name.value,
        "pass" -> password,
        "challstr" -> challStr.value
      )).asInstanceOf[Request[F]]
    ).map(body => body.fromJson[LoginResponse].left.map(msg => ProtocolError.InvalidInput(body, msg)))
    .rethrow
    .flatTap(data => sendMessage(AuthCommand.Trn(name, 0, data.assertion)))

  override def loginGuest(challStr: ChallStr)(name: Username): F[String] =
    client.expect[String](
      Request(
        method = Method.POST,
        uri = Uri.unsafeFromString("https://play.pokemonshowdown.com/action.php")
      ).withEntity(UrlForm(
        "act" -> "getassertion",
        "userid" -> name.value,
        "challstr" -> challStr.value
      )).asInstanceOf[Request[F]]
    ).flatTap(assertion => sendMessage(AuthCommand.Trn(name, 0, assertion)))