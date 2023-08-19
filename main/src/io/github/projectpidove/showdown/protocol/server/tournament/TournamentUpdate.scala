package io.github.projectpidove.showdown.protocol.server.tournament

import io.github.iltotore.iron.zioJson.given
import io.github.projectpidove.showdown.{Count, FormatName}
import io.github.projectpidove.showdown.protocol.MessageDecoder
import io.github.projectpidove.showdown.user.Username
import zio.json.*

/**
 * A tournament update. Each parameter is optional (only present if the state changed).
 *
 * @param format the format of the battles of the tournament
 * @param teambuilderFormat the format of the tournament used for teambuilding
 * @param isStarted whether the tournament started or not
 * @param isJoined whether the current (logged in) user joined the tournament or not
 * @param generator the bracket generator of the tournament
 * @param playerCap the maximum count of the player
 * @param bracketData the current state of the bracket
 * @param challenges the list of opponents the current user can challenge
 * @param challengesBys the list of opponents who can challenge the current user
 * @param challenged the list of opponents currently challenging the user
 * @param challenging the list of opponents the user is challenging
 */
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
