package io.github.projectpidove.showdown.protocol.server

import io.github.projectpidove.showdown.protocol.{MessageDecoder, ProtocolError}
import io.github.projectpidove.showdown.protocol.MessageDecoder.toInvalidInput
import io.github.projectpidove.showdown.room.ChatContent

enum ChoiceError:
  case Invalid(message: ChatContent)
  case Unavailable(message: ChatContent)

object ChoiceError:

  given (using chatDecoder: MessageDecoder[ChatContent]): MessageDecoder[ChoiceError] = chatDecoder.mapEither:
    case s"[Invalid choice] $message" =>
      ChatContent
        .either(message)
        .map(ChoiceError.Invalid.apply)
        .toInvalidInput(message)

    case s"[Unavailable choice] $message" =>
      ChatContent
        .either(message)
        .map(ChoiceError.Unavailable.apply)
        .toInvalidInput(message)

    case value => Left(ProtocolError.InvalidInput(value.toString, "Invalid choice error"))