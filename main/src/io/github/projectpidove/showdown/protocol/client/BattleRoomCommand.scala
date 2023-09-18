package io.github.projectpidove.showdown.protocol.client

import io.github.projectpidove.showdown.protocol.MessageEncoder
import io.github.projectpidove.showdown.FormatName
import io.github.projectpidove.showdown.room.{ChatContent, RoomId, given}
import io.github.projectpidove.showdown.user.{Username, given}

/**
 * A command bound to a battle room.
 */
enum BattleRoomCommand derives MessageEncoder:

  /**
   * Save the replay of the match.
   */
  case SaveReplay

  /**
   * Make the room secret.
   *
   * @see [[BattleRoomCommand.PublicRoom]]
   */
  case SecretRoom

  /**
   * Set the visibility of the room.
   *
   * @param isOn `true` for hidden, `false` otherwise.
   */
  case HideRoom(isOn: Boolean)

  /**
   * Make the room public.
   *
   * @see [[BattleRoomCommand.SecretRoom]]
   */
  case PublicRoom

  /**
   * Make the room invite-only.
   *
   * @param isOn `true` make the room invite-only, `false` otherwise.
   */
  case InviteOnly(isOn: Boolean)

  /**
   * Make the next battle invite-only.
   *
   * @param isOn `true` make the next battle invite-only, `false` otherwise.
   */
  case InviteOnlyNext(isOn: Boolean)

  /**
   * Invite the given user to the given room.
   *
   * @param user the user to invite or all PMed player if absent
   * @param room the room to invite to, or the current room of the current user if absent
   */
  case Invite(user: Option[Username], roomName: Option[RoomId])

  /**
   * Enable the timer
   * 
   * @param isOn `true` to enable the timer, `false` to disable.
   */
  case Timer(isOn: Boolean)

  /**
   * Forfeit the current battle.
   */
  case Forfeit

  /**
   * Select a choice.
   */
  case Choose(choice: BattleChoice)