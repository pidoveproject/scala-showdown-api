package io.github.pidoveproject.showdown.battle

import io.github.pidoveproject.showdown.user.{AvatarName, Username}

/**
 * A currently battling player.
 *
 * @param number this player's battle position
 * @param name this player's username
 * @param avatar this player's avatar
 * @param rating this player's rating in the currently played format
 * @param team this player's pokemon team
 */
case class Player(
     number: PlayerNumber,
     name: Option[Username],
     avatar: Option[AvatarName],
     rating: Option[Rating],
     team: Option[PlayerTeam] = None
)