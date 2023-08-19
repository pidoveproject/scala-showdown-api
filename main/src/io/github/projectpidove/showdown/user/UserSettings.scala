package io.github.projectpidove.showdown.user

import io.github.projectpidove.showdown.protocol.MessageDecoder
import zio.json.*

/**
 * Represents various settings of a user.
 *
 * @param blockChallenges whether this user blocks challenges from others or not
 * @param blockPMs whether this user blocks private messages from others or not
 * @param ignoreTickets whether this user ignore tickets or not
 * @param hideBattlesFromTrainerCard whether this user hides its trainer card from battles or not
 * @param blockInvites whether this user blocks invites from others or not
 * @param doNotDisturb whether this user receives notifications or not
 * @param blockFriendRequests whether this user blocks friend requests from others or not
 * @param allowFriendNotifications whether this user allows notifications from friends or not
 * @param displayBattlesToFriends whether this user's friends can see their battle or not
 * @param hideLogins ???
 * @param hiddenNextBattle ???
 * @param inviteOnlyNextBattle ???
 * @param language the language of this user's UI
 */
case class UserSettings(
    blockChallenges: Boolean,
    blockPMs: Boolean,
    ignoreTickets: Boolean,
    hideBattlesFromTrainerCard: Boolean,
    blockInvites: Boolean,
    doNotDisturb: Boolean,
    blockFriendRequests: Boolean,
    allowFriendNotifications: Boolean,
    displayBattlesToFriends: Boolean,
    hideLogins: Boolean,
    hiddenNextBattle: Boolean,
    inviteOnlyNextBattle: Boolean,
    language: String
) derives JsonDecoder, JsonEncoder

object UserSettings:

  given MessageDecoder[UserSettings] = MessageDecoder.fromJson
