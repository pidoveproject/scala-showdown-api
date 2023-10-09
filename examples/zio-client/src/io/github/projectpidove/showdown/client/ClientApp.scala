package io.github.projectpidove.showdown.client

import io.github.projectpidove.showdown.ZIOShowdownConnection
import io.github.projectpidove.showdown.battle.Battle
import io.github.projectpidove.showdown.room.RoomId

case class ClientApp(debugging: Boolean = false, currentRoom: Option[RoomId] = None):

  def currentBattle: ConnectionTask[Option[Battle]] =
    for
      currentState <- ZIOShowdownConnection.currentState
    yield
      currentRoom
        .flatMap(currentState.joinedRooms.get)
        .flatMap(_.battle)