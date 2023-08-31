package io.github.projectpidove.showdown.protocol

/**
 * A protocol-bound error
 */
enum ProtocolError(message: String) extends Throwable(message):

  /**
   * The input has been exhausted.
   */
  case InputExhausted(data: String, length: Int) extends ProtocolError(s"Input exhausted. Length: $length")

  /**
   * Tried to decode an invalid input.
   */
  case InvalidInput(input: String, message: String) extends ProtocolError(s"Invalid input: $input, $message")

  /**
   * The input is in invalid format.
   */
  case InvalidFormat(message: String) extends ProtocolError(s"Invalid format: $message")

  /**
   * Miscellaneous error.
   */
  case Miscellaneous(message: String) extends ProtocolError(message)

  /**
   * The ProtocolError has been caused by a thrown error.
   */
  case Thrown(cause: Throwable) extends ProtocolError(cause.getMessage)

  /**
   * The connection has been closed.
   */
  case ConnectionClosed extends ProtocolError("Connection closed")

  /**
   * The authentification process failed.
   */
  case AuthentificationFailed(message: String) extends ProtocolError(s"Authentification failed: $message")
