package io.github.projectpidove.showdown.protocol

import zio.json.*

case class LoginResponse(
                          @jsonField("actionsuccess") actionSuccess: Boolean,
                          assertion: String,
                          @jsonField("curuser") currentUser: CurrentUser
                        )derives JsonDecoder