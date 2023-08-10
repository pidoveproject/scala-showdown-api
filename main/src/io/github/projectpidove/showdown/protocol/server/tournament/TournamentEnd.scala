package io.github.projectpidove.showdown.protocol.server.tournament

import io.github.iltotore.iron.zioJson.given
import io.github.projectpidove.showdown.FormatName
import io.github.projectpidove.showdown.protocol.MessageDecoder
import io.github.projectpidove.showdown.user.Username
import zio.json.JsonDecoder

case class TournamentEnd(
    results: List[Username],
    format: FormatName,
    generator: TournamentGenerator,
    bracketData: BracketData
) derives JsonDecoder

object TournamentEnd:

  given MessageDecoder[TournamentEnd] = MessageDecoder.fromJson
