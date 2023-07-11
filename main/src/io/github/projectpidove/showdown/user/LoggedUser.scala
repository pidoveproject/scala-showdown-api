package io.github.projectpidove.showdown.user

case class LoggedUser(name: String, avatar: String, isGuest: Boolean, settings: UserSettings)
