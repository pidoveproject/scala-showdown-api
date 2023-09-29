package io.github.projectpidove.showdown.battle

import io.github.projectpidove.showdown.user.{AvatarName, Username}

case class Player(
     number: PlayerNumber,
     name: Option[Username],
     avatar: Option[AvatarName],
     rating: Option[Rating],
     team: Option[PlayerTeam] = None
)