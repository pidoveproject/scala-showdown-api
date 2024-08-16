package io.github.pidoveproject.showdown.testing.protocol.client

import io.github.iltotore.iron.*
import io.github.pidoveproject.showdown.battle.{MoveSlot, TeamSlot}
import io.github.pidoveproject.showdown.protocol.MessageEncoder
import io.github.pidoveproject.showdown.protocol.client.*
import io.github.pidoveproject.showdown.room.RoomId
import io.github.pidoveproject.showdown.testing.protocol.*
import io.github.pidoveproject.showdown.user.Username
import utest.*

object BattleRoomSuite extends TestSuite:

  val tests = Tests:

    val encoder = summon[MessageEncoder[BattleRoomCommand]]

    test("saveReplay") - assertEncode(encoder, BattleRoomCommand.SaveReplay, List("savereplay"))
    test("secretRoom") - assertEncode(encoder, BattleRoomCommand.SecretRoom, List("secretroom"))
    test("hideRoom") - assertEncode(encoder, BattleRoomCommand.HideRoom(true), List("hideroom", "on"))
    test("publicRoom") - assertEncode(encoder, BattleRoomCommand.PublicRoom, List("publicroom"))
    test("inviteOnly") - assertEncode(encoder, BattleRoomCommand.InviteOnly(true), List("inviteonly", "on"))
    test("inviteOnlyNext") - assertEncode(encoder, BattleRoomCommand.InviteOnlyNext(true), List("inviteonlynext", "on"))
    test("invite") - assertEncode(
      encoder,
      BattleRoomCommand.Invite(Some(Username("Il_totore")), Some(RoomId("gen9ou1234"))),
      List("invite", "Il_totore", "gen9ou1234")
    )
    test("timer") - assertEncode(encoder, BattleRoomCommand.Timer(true), List("timer", "on"))
    test("forfeit") - assertEncode(encoder, BattleRoomCommand.Forfeit, List("forfeit"))
    test("choice"):
      test("switch") - assertEncode(encoder, BattleRoomCommand.Choose(ChoiceResponse(BattleChoice.Switch(TeamSlot(1)))), List("choose", "switch 1"))
      test("withId") - assertEncode(
        encoder,
        BattleRoomCommand.Choose(ChoiceResponse(BattleChoice.Switch(TeamSlot(1)), Some(1))),
        List("choose", "switch 1|1")
      )
      test("move") - assertEncode(
        encoder,
        BattleRoomCommand.Choose(ChoiceResponse(BattleChoice.Move(MoveSlot(1), None, None))),
        List("choose", "move 1")
      )
