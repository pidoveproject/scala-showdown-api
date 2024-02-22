package io.github.pidoveproject.showdown.protocol.server.choice

import io.github.pidoveproject.showdown.protocol.MessageDecoder.toInvalidInput
import io.github.pidoveproject.showdown.protocol.{MessageDecoder, ProtocolError}
import io.github.pidoveproject.showdown.room.ChatContent

/**
 * An error message about a wrong choice.
 */
enum ChoiceError:

  /**
   * The sent decision is invalid.
   * 
   * @param message the error message
   */
  case Invalid(message: ChatContent)

  /**
   * The sent decision is unavailable (e.g due to a ability like Magnet Pull).
   * 
   * @param message the error message
   */
  case Unavailable(message: ChatContent)

  /**
   * A miscellaneous error.
   */
  case Miscellaneous(message: ChatContent)

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

    case message =>
      Right(ChoiceError.Miscellaneous(message))