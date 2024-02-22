package io.github.pidoveproject.showdown.protocol.server.choice

import zio.json.*

/**
 * Choice information on the active side.
 *
 * @param moves the available moves
 */
case class ActiveChoice(moves: List[MoveChoice]) derives JsonDecoder
