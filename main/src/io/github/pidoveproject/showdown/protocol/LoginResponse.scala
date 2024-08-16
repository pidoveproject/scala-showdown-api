package io.github.pidoveproject.showdown.protocol

import io.github.iltotore.iron.zioJson.given
import zio.json.*

case class LoginResponse(
    @jsonField("actionsuccess") actionSuccess: Boolean,
    assertion: Assertion,
    @jsonField("curuser") currentUser: CurrentUser
) derives JsonDecoder
