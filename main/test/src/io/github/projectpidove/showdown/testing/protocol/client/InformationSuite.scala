package io.github.projectpidove.showdown.testing.protocol.client

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.{FormatName, Generation}
import io.github.projectpidove.showdown.protocol.MessageEncoder
import io.github.projectpidove.showdown.protocol.client.*
import io.github.projectpidove.showdown.room.{ChatMessage, RoomId}
import io.github.projectpidove.showdown.team.{SpeciesName, Tier}
import io.github.projectpidove.showdown.testing.protocol.*
import io.github.projectpidove.showdown.user.Username
import utest.*

object InformationSuite extends TestSuite:

  val tests = Tests:

    val encoder = summon[MessageEncoder[InformationCommand]]

    test("groups") - assertEncode(encoder, InformationCommand.Groups(Some(GroupTarget.Global)), List("groups", "global"))
    test("faq") - assertEncode(encoder, InformationCommand.Faq(Some("ladder")), List("faq", "ladder"))
    test("rules") - assertEncode(encoder, InformationCommand.Rules(Some("url")), List("rules", "url"))
    test("intro") - assertEncode(encoder, InformationCommand.Intro, List("intro"))
    test("formatsHelp") - assertEncode(encoder, InformationCommand.FormatsHelp(Some(FormatName("gen9ou"))), List("formatshelp", "gen9ou"))
    test("analysis") - assertEncode(
      encoder,
      InformationCommand.Analysis(SpeciesName("Heracross"), Some(Generation(7)), Some(Tier("ou"))),
      List("analysis", "Heracross", "7", "ou")
    )
    test("otherMetas") - assertEncode(encoder, InformationCommand.OtherMetas, List("othermetas"))
    test("punishments") - assertEncode(encoder, InformationCommand.Punishments, List("punishments"))
    test("calc") - assertEncode(encoder, InformationCommand.Calc, List("calc"))
    test("rCalc") - assertEncode(encoder, InformationCommand.RCalc, List("rcalc"))
    test("bsCalc") - assertEncode(encoder, InformationCommand.BsCalc, List("bscalc"))
    test("git") - assertEncode(encoder, InformationCommand.Git, List("git"))
    test("cap") - assertEncode(encoder, InformationCommand.Cap, List("cap"))
    test("roomHelp") - assertEncode(encoder, InformationCommand.RoomHelp, List("roomhelp"))
    test("roomFaq") - assertEncode(encoder, InformationCommand.RoomFaq(Some("ads")), List("roomfaq", "ads"))