package io.github.projectpidove.showdown.user

import io.github.projectpidove.showdown.protocol.MessageDecoder
import zio.json.*

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
