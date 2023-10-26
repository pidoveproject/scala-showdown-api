package io.github.projectpidove.showdown.client

import io.github.projectpidove.showdown.room.RoomId
import io.github.projectpidove.showdown.user.Username

enum TabChoice:
  case PrivateMessage(user: Username)
  case Room(room: RoomId)