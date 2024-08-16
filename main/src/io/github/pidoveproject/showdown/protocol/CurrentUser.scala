package io.github.pidoveproject.showdown.protocol

import zio.json.*

@jsonMemberNames(CustomCase(_.toLowerCase))
case class CurrentUser(loggedIn: Boolean, username: String, userId: String) derives JsonDecoder
