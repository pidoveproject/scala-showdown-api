package io.github.projectpidove.showdown.protocol.server.query

import io.github.iltotore.iron.constraint.string.*
import io.github.iltotore.iron.zioJson.given
import io.github.projectpidove.showdown.protocol.MessageDecoder
import io.github.projectpidove.showdown.room.AvatarName
import zio.json.*

@jsonMemberNames(CustomCase(_.toLowerCase))
case class UserInfo(
    id: String,
    userId: String,
    name: String,
    avatar: AvatarName,
    group: String,
    autoConfirmed: Boolean,
    rooms: Map[String, Map[String, String]]
) derives JsonDecoder

object UserInfo:

  given MessageDecoder[UserInfo] = MessageDecoder.fromJson