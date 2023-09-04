package io.github.projectpidove.showdown.testing.protocol.client

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.FormatName
import io.github.projectpidove.showdown.protocol.MessageEncoder
import io.github.projectpidove.showdown.protocol.client.*
import io.github.projectpidove.showdown.room.{ChatContent, RoomId}
import io.github.projectpidove.showdown.testing.protocol.*
import io.github.projectpidove.showdown.user.{AvatarName, Username}
import utest.*

object OptionSuite extends TestSuite:

  val tests = Tests:

    val encoder = summon[MessageEncoder[OptionCommand]]

    test("nick") - assertEncode(encoder, OptionCommand.Nick(Some(Username("Il_totore"))), List("nick", "Il_totore"))
    test("avatar") - assertEncode(encoder, OptionCommand.Avatar(AvatarName("kimonogirl")), List("avatar", "kimonogirl"))
    test("ignore") - assertEncode(encoder, OptionCommand.Ignore(Username("LeFanDeMeganium")), List("ignore", "LeFanDeMeganium"))
    test("status") - assertEncode(encoder, OptionCommand.Status("AFK"), List("status", "AFK"))
    test("clearStatus") - assertEncode(encoder, OptionCommand.ClearStatus, List("clearstatus"))
    test("away") - assertEncode(encoder, OptionCommand.Away, List("away"))
    test("busy") - assertEncode(encoder, OptionCommand.Busy, List("busy"))
    test("doNotDisturb") - assertEncode(encoder, OptionCommand.DoNotDisturb, List("donotdisturb"))
    test("back") - assertEncode(encoder, OptionCommand.Back, List("back"))
    test("timestamps") - assertEncode(encoder, OptionCommand.Timestamps(TimestampTarget.All, TimeInterval.Minutes), List("timestamps", "all", "minutes"))
    test("showJoins") - assertEncode(encoder, OptionCommand.ShowJoins(Some(RoomId("lobby"))), List("showjoins", "lobby"))
    test("hideJoins") - assertEncode(encoder, OptionCommand.HideJoins(Some(RoomId("lobby"))), List("hidejoins", "lobby"))
    test("blockChallenges") - assertEncode(encoder, OptionCommand.BlockChallenges, List("blockchallenges"))
    test("unblockChallenges") - assertEncode(encoder, OptionCommand.UnblockChallenges, List("unblockchallenges"))
    test("blockPMs") - assertEncode(encoder, OptionCommand.BlockPrivateMessages(Some(PrivateMessageGroup.Friends)), List("blockpms", "friends"))
