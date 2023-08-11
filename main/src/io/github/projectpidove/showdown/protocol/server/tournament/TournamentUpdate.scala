package io.github.projectpidove.showdown.protocol.server.tournament

import io.github.iltotore.iron.zioJson.given
import io.github.projectpidove.showdown.{Count, FormatName}
import io.github.projectpidove.showdown.protocol.MessageDecoder
import io.github.projectpidove.showdown.user.Username
import zio.json.*

case class TournamentUpdate(
    format: Option[FormatName] = None,
    teambuilderFormat: Option[FormatName] = None,
    isStarted: Option[Boolean] = None,
    isJoined: Option[Boolean] = None,
    generator: Option[TournamentGenerator] = None,
    playerCap: Option[Count] = None,
    bracketData: Option[BracketData] = None,
    challenges: Option[List[Username]] = None,
    challengesBys: Option[List[Username]] = None,
    challenged: Option[List[Username]] = None,
    challenging: Option[List[Username]] = None
) derives JsonDecoder

object TournamentUpdate:

  given MessageDecoder[TournamentUpdate] = MessageDecoder.fromJson
