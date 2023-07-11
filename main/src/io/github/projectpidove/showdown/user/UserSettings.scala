package io.github.projectpidove.showdown.user

import java.util.Locale

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
    language: Locale
)
