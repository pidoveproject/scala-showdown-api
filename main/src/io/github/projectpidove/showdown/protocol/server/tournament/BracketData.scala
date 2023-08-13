package io.github.projectpidove.showdown.protocol.server.tournament

import io.github.iltotore.iron.zioJson.given
import io.github.projectpidove.showdown.user.Username
import zio.json.*

case class BracketData(
    @jsonField("type") bracketType: BracketType,
    rootNode: Option[BracketNode] = None,
    users: Option[List[Username]] = None
) derives JsonDecoder
