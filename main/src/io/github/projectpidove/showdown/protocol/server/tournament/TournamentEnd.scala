package io.github.projectpidove.showdown.protocol.server.tournament

import io.github.iltotore.iron.zioJson.given
import io.github.projectpidove.showdown.FormatName
import io.github.projectpidove.showdown.protocol.MessageDecoder
import io.github.projectpidove.showdown.user.Username
import zio.json.JsonDecoder

/**
 * A tournament ending.
 *
 * @param results the winner of the tournament
 * @param format the format of the tournament
 * @param generator the bracket generator of the tournament
 * @param bracketData the final bracket of the tournament
 */
case class TournamentEnd(
    results: List[List[Username]],
    format: FormatName,
    generator: TournamentGenerator,
    bracketData: BracketData
) derives JsonDecoder

object TournamentEnd:

  given MessageDecoder[TournamentEnd] = MessageDecoder.fromJson
