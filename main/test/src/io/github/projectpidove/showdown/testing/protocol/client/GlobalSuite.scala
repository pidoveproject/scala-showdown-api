package io.github.projectpidove.showdown.testing.protocol.client

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.protocol.{MessageEncoder, MessageName}
import io.github.projectpidove.showdown.protocol.client.*
import io.github.projectpidove.showdown.room.{ChatContent, RoomId}
import io.github.projectpidove.showdown.FormatName
import io.github.projectpidove.showdown.testing.protocol.*
import io.github.projectpidove.showdown.user.Username
import utest.*

object GlobalSuite extends TestSuite:

  val tests = Tests:

    val encoder = summon[MessageEncoder[GlobalCommand]]

    test("report") - assertEncode(
      encoder,
      GlobalCommand.Report(Username("LeFanDeMeganium"), "Cheating >:("),
      List("report", "LeFanDeMeganium", "Cheating >:(")
    )

    test("msg") - assertEncode(
      encoder,
      GlobalCommand.Msg(Username("LeFanDeMeganium"), ChatContent("Wanna rematch?")),
      List("msg", "LeFanDeMeganium", "Wanna rematch?")
    )

    test("reply") - assertEncode(encoder, GlobalCommand.Reply(ChatContent("Sure I'll challenge you")), List("reply", "Sure I'll challenge you"))
    test("logOut") - assertEncode(encoder, GlobalCommand.LogOut, List("logout"))
    test("challenge") - assertEncode(
      encoder,
      GlobalCommand.Challenge(Username("Il_totore"), FormatName("gen9ou")),
      List("challenge", "Il_totore", "gen9ou")
    )

    test("search") - assertEncode(encoder, GlobalCommand.Search(FormatName("gen9ou")), List("search", "gen9ou"))
    test("rating") - assertEncode(encoder, GlobalCommand.Rating(Some(Username("Il_totore"))), List("rating", "Il_totore"))
    test("whoIs") - assertEncode(encoder, GlobalCommand.WhoIs(Some(Username("Il_totore"))), List("whois", "Il_totore"))
    test("user") - assertEncode(encoder, GlobalCommand.User(Some(Username("Il_totore"))), List("user", "Il_totore"))
    test("join") - assertEncode(encoder, GlobalCommand.Join(RoomId("gen9ou1234")), List("join", "gen9ou1234"))
    test("leave") - assertEncode(encoder, GlobalCommand.Leave(Some(RoomId("gen9ou1234"))), List("leave", "gen9ou1234"))
    test("join") - assertEncode(encoder, GlobalCommand.UserAuth(Username("Il_totore")), List("userauth", "Il_totore"))
    test("roomAuth") - assertEncode(encoder, GlobalCommand.RoomAuth(RoomId("gen9ou1234")), List("roomauth", "gen9ou1234"))
    test("query"):
      test("userDetails") - assertEncode(encoder, GlobalCommand.Query(QueryRequest.UserDetails(Username("Il_totore"))), List("query", "userdetails", "Il_totore"))
      test("battleRooms") - assertEncode(encoder, GlobalCommand.Query(QueryRequest.BattleRooms()), List("query", "roomlist"))
      test("chatRooms") - assertEncode(encoder, GlobalCommand.Query(QueryRequest.ChatRooms()), List("query", "rooms"))