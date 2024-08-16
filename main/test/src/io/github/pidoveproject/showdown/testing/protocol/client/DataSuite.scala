package io.github.pidoveproject.showdown.testing.protocol.client

import io.github.iltotore.iron.*
import io.github.pidoveproject.showdown.protocol.MessageEncoder
import io.github.pidoveproject.showdown.protocol.client.*
import io.github.pidoveproject.showdown.protocol.client.DataCommand.EVBoost
import io.github.pidoveproject.showdown.team.*
import io.github.pidoveproject.showdown.testing.protocol.*
import io.github.pidoveproject.showdown.{FormatName, Generation}
import utest.*

object DataSuite extends TestSuite:

  val tests = Tests:

    val encoder = summon[MessageEncoder[DataCommand]]

    test("data"):
      test("species") - assertEncode(encoder, DataCommand.Data(SpeciesName("Girafarig")), List("data", "Girafarig"))
      test("item") - assertEncode(encoder, DataCommand.Data(ItemName("Choice Specs")), List("data", "Choice Specs"))
      test("move") - assertEncode(encoder, DataCommand.Data(MoveName("Twin Beam")), List("data", "Twin Beam"))
      test("ability") - assertEncode(encoder, DataCommand.Data(AbilityName("Armor Tail")), List("data", "Armor Tail"))
      test("nature") - assertEncode(encoder, DataCommand.Data(Nature.Modest), List("data", "modest"))

    test("learn"):
      test("empty") - assertEncode(
        encoder,
        DataCommand.Learn(None, SpeciesName("Girafarig"), List(MoveName("Psybeam"))),
        List("learn", "Girafarig", "Psybeam")
      )
      test("generation") - assertEncode(
        encoder,
        DataCommand.Learn(Some(Generation(2)), SpeciesName("Girafarig"), List(MoveName("Psybeam"))),
        List("learn", "gen2", "Girafarig", "Psybeam")
      )
      test("format") - assertEncode(
        encoder,
        DataCommand.Learn(Some(FormatName("gen2ou")), SpeciesName("Girafarig"), List(MoveName("Psybeam"))),
        List("learn", "gen2ou", "Girafarig", "Psybeam")
      )

    test("statCalc") -
      assertEncode(
        encoder,
        DataCommand.StatCalc(
          level = Some(Level(100)),
          baseStat = 100,
          iv = Some(IV(31)),
          ev = Some(EVBoost.Buffed()),
          modifier = Some(Modifier("scarf"))
        ),
        List("statcalc", "lv100", "100", "31iv", "252ev+", "scarf")
      )

    test("effectiveness"):
      test("moveOnPokemon") - assertEncode(
        encoder,
        DataCommand.Effectiveness(MoveName("Earthquake"), SpeciesName("Cinderace")),
        List("effectiveness", "Earthquake", "Cinderace")
      )
      test("moveOnPokemon") - assertEncode(encoder, DataCommand.Effectiveness(Type.Ground, Type.Fire), List("effectiveness", "ground", "fire"))

    test("weakness"):
      test("species") - assertEncode(encoder, DataCommand.Weakness(SpeciesName("Meganium")), List("weakness", "Meganium"))
      test("type") - assertEncode(encoder, DataCommand.Weakness(Type.Grass), List("weakness", "grass"))
      test("types") - assertEncode(encoder, DataCommand.Weakness((Type.Grass, Type.Bug)), List("weakness", "grass", "bug"))

    test("coverage") - assertEncode(
      encoder,
      DataCommand.Coverage(List(MoveName("Close Combat"), MoveName("Spectral Thief"))),
      List("coverage", "Close Combat", "Spectral Thief")
    )
