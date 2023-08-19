package io.github.projectpidove.showdown.protocol.server.tournament

import io.github.iltotore.iron.zioJson.given
import io.github.projectpidove.showdown.user.Username
import zio.json.*

/**
 * The bracket data of a tournament
 *
 * @param bracketType the type of the bracket
 * @param rootNode the root of the bracket
 * @param users the participants of the tournament
 */
case class BracketData(
    @jsonField("type") bracketType: BracketType,
    rootNode: Option[BracketNode] = None,
    users: Option[List[Username]] = None
) derives JsonDecoder
