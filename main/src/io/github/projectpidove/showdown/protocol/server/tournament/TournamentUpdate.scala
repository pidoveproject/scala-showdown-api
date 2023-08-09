package io.github.projectpidove.showdown.protocol.server.tournament

import io.github.iltotore.iron.zioJson.given
import io.github.projectpidove.showdown.{Count, FormatName}
import io.github.projectpidove.showdown.protocol.MessageDecoder
import io.github.projectpidove.showdown.user.Username
import zio.json.*

case class TournamentUpdate(
    format: Option[FormatName],
    teambuilderFormat: Option[FormatName],
    isStarted: Option[Boolean],
    isJoined: Option[Boolean],
    generator: Option[TournamentGenerator],
    playerCap: Option[Count],
    bracketData: Option[BracketData],
    challenges: Option[List[Username]],
    challengesBys: Option[List[Username]],
    challenged: Option[List[Username]],
    challenging: Option[List[Username]]
) derives JsonDecoder

object TournamentUpdate:

  given MessageDecoder[TournamentUpdate] = MessageDecoder.fromJson
