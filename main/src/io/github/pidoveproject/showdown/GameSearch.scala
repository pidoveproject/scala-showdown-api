package io.github.pidoveproject.showdown

import io.github.iltotore.iron.zioJson.given
import io.github.pidoveproject.showdown.protocol.MessageDecoder
import zio.json.*

/**
 * Informations about the current search.
 *
 * @param searching the searched formats
 * @param games the found games
 */
case class GameSearch(searching: List[FormatName], games: Map[String, String]) derives JsonDecoder

object GameSearch:

  private given JsonDecoder[Map[String, String]] =
    JsonDecoder
      .option(using JsonDecoder.map[String, String])
      .map(_.getOrElse(Map.empty))

  given MessageDecoder[GameSearch] = MessageDecoder.fromJson
  
  val empty: GameSearch = GameSearch(List.empty, Map.empty)
