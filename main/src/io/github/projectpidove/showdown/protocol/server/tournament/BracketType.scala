package io.github.projectpidove.showdown.protocol.server.tournament

import zio.json.{JsonDecoder, jsonHint}

enum BracketType:
  case Tree

object BracketType:

  def fromName(name: String): Option[BracketType] =
    BracketType.values.find(_.toString equalsIgnoreCase name)

  given JsonDecoder[BracketType] =
    JsonDecoder
      .string
      .mapOrFail(BracketType.fromName(_).toRight("Invalid bracket type"))
