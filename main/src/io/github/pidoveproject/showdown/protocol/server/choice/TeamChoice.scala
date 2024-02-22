package io.github.pidoveproject.showdown.protocol.server.choice

import io.github.iltotore.iron.zioJson.given
import io.github.pidoveproject.showdown.battle.{PlayerNumber, given}
import io.github.pidoveproject.showdown.user.Username
import zio.json.*

/**
 * Team-related choices.
 *
 * @param name the name of the team (usually the owner's name)
 * @param player the number of the team owner
 * @param pokemon the pokemon choices
 */
case class TeamChoice(
    name: Username,
    @jsonField("id") player: PlayerNumber,
    pokemon: List[PokemonChoice]
) derives JsonDecoder