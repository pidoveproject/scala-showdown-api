package io.github.pidoveproject.showdown.protocol.server.choice

import io.github.pidoveproject.showdown.protocol.MessageDecoder
import io.github.pidoveproject.showdown.json.nonEmptyListOrEmpty
import zio.json.*

/**
 * A choice request sent by the server.
 * 
 * @param active the choices related to the active pokemon
 * @param team the choice related to the team (e.g switching)
 * @param requestId the id of the request, used to ensure the sent decision is not confused with another one
 */
case class ChoiceRequest(
    active: List[ActiveChoice],
    @jsonField("side") team: TeamChoice,
    @jsonField("rqid") requestId: Option[Int]
) derives JsonDecoder

object ChoiceRequest:

  given MessageDecoder[ChoiceRequest] = MessageDecoder.fromJson
