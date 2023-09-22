package io.github.projectpidove.showdown.battle

import io.github.projectpidove.showdown.user.{AvatarName, Username}

case class Player(number: PlayerNumber, name: Username, avatar: AvatarName, rating: Rating, team: Option[PlayerTeam] = None)
