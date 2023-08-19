package io.github.projectpidove.showdown.protocol

/**
 * A protocol-bound error
 */
enum ProtocolError:

  /**
   * The input has been exhausted.
   */
  case InputExhausted(data: String, length: Int)

  /**
   * Tried to decode an invalid input.
   */
  case InvalidInput(input: String, message: String)

  /**
   * The input is in invalid format.
   */
  case InvalidFormat(message: String)

  /**
   * Miscellaneous error.
   */
  case Miscellaneous(message: String)

  /**
   * The ProtocolError has been caused by a thrown error.
   */
  case Thrown(cause: Throwable)
