package io.github.pidoveproject.showdown.protocol.server.tournament

import zio.json.{JsonDecoder, jsonHint}

/**
 * The type of a bracket
 */
enum BracketType:
  case Tree

object BracketType:

  /**
   * Get the [[BracketType]] from its name.
   * @param name the name of the bracket type to retrieve
   * @return the corresponding bracket type
   */
  def fromName(name: String): Option[BracketType] =
    BracketType.values.find(_.toString equalsIgnoreCase name)

  given JsonDecoder[BracketType] =
    JsonDecoder
      .string
      .mapOrFail(BracketType.fromName(_).toRight("Invalid bracket type"))
