package io.github.pidoveproject.showdown.protocol.server.choice

import io.github.iltotore.iron.*
import io.github.iltotore.iron.zioJson.given
import io.github.pidoveproject.showdown.battle.PP
import io.github.pidoveproject.showdown.team.MoveName
import zio.json.*

/**
 * Represent a selectable move.
 *
 * @param name the name of the move
 * @param id the internal id of the move (lower-cased and without spaces)
 * @param pp the remaining PP of the move
 * @param maxPP the max PP of the move
 * @param range the targeting range of the move
 * @param disabled whether this move is disabled or usable
 */
case class MoveChoice(
    @jsonField("move") name: MoveName,
    id: String,
    pp: PP,
    @jsonField("maxpp") maxPP: PP,
    @jsonField("target") range: MoveRange,
    disabled: Boolean
) derives JsonDecoder
