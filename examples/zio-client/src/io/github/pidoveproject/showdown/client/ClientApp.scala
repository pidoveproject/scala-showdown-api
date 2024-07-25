package io.github.pidoveproject.showdown.client

import io.github.pidoveproject.showdown.ShowdownData
import io.github.pidoveproject.showdown.battle.Battle
import io.github.pidoveproject.showdown.room.RoomId

/**
 * Client state.
 * 
 * @param debugging whether the client is in debug mode (print incoming messages) or not.
 * @param currentRoom the currently used room (where the client sends messages)
 */
case class ClientApp(currentState: ShowdownData, debugging: Boolean = false, currentRoom: Option[RoomId] = None):

  /**
   * The currently viewed battle aka the battle of the current room.
   */
  def currentBattle: Option[Battle] =
      currentRoom
        .flatMap(currentState.joinedRooms.get)
        .flatMap(_.battle)