package io.github.pidoveproject.showdown.protocol.client

import io.github.pidoveproject.showdown.protocol.{MessageEncoder, ProtocolError}

case class ChoiceResponse(choice: BattleChoice, requestId: Option[Int] = None)

object ChoiceResponse:

  given (using choiceEncoder: MessageEncoder[BattleChoice], requestEncoder: MessageEncoder[Option[Int]]): MessageEncoder[ChoiceResponse] with
    override def encode(value: ChoiceResponse): Either[ProtocolError, List[String]] =
      for
        encodedChoice <- choiceEncoder.encode(value.choice)
        encodedRequest <- requestEncoder.encode(value.requestId)
      yield
        if encodedRequest.isEmpty then List(encodedChoice.mkString(" "))
        else List((encodedChoice.dropRight(1) :+ s"${encodedChoice.last}|${encodedRequest.mkString}").mkString(" "))