package io.github.projectpidove.showdown

import io.github.projectpidove.showdown.protocol.MessageDecoder
import zio.json.*

case class GameSearch(searching: List[String], games: Map[String, String]) derives JsonDecoder

object GameSearch:

  private given JsonDecoder[Map[String, String]] =
    JsonDecoder
      .option(using JsonDecoder.map[String, String])
      .map(_.getOrElse(Map.empty))

  given MessageDecoder[GameSearch] = MessageDecoder.fromJson
