package io.github.projectpidove.showdown.client

import io.github.projectpidove.showdown.ZIOShowdownConnection
import io.github.projectpidove.showdown.battle.Battle
import io.github.projectpidove.showdown.room.RoomId

/**
 * Client state.
 * 
 * @param debugging whether the client is in debug mode (print incoming messages) or not.
 * @param currentRoom the currently used room (where the client sends messages)
 */
case class ClientApp(debugging: Boolean = false, currentRoom: Option[RoomId] = None):

  /**
   * The currently viewed battle aka the battle of the current room.
   */
  def currentBattle: ConnectionTask[Option[Battle]] =
    for
      currentState <- ZIOShowdownConnection.currentState
    yield
      currentRoom
        .flatMap(currentState.joinedRooms.get)
        .flatMap(_.battle)