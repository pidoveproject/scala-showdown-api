package io.github.pidoveproject.showdown.protocol

import zio.json.*
import io.github.iltotore.iron.zioJson.given
import io.github.pidoveproject.showdown.user.Username

@jsonMemberNames(CustomCase(_.toLowerCase))
case class CurrentUser(loggedIn: Boolean, username: Username, userId: String) derives JsonDecoder
