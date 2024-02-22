package io.github.pidoveproject.showdown.protocol.server.query

import io.github.iltotore.iron.constraint.string.*
import io.github.iltotore.iron.zioJson.given
import io.github.pidoveproject.showdown.protocol.MessageDecoder
import io.github.pidoveproject.showdown.user.AvatarName
import zio.json.*

/**
 * Details about a user.
 *
 * @param id the user's id
 * @param userId the user's user id (usually the id, lower-cased and without spaces and special chars)
 * @param name the user's name
 * @param avatar the user's avatar
 * @param group the user's group/role
 * @param autoConfirmed true if this user has been authenticated automatically
 * @param rooms the list of sub-rooms
 */
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
