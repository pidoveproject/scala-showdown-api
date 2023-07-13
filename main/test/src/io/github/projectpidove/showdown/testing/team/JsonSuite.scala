package io.github.projectpidove.showdown.testing.team

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.projectpidove.showdown.team.*
import utest.*
import zio.json.*

object JsonSuite extends TestSuite:

  def assertEncode[T : JsonEncoder](value: T, result: String): Unit = assert(value.toJson == result)
  def assertEncodeStr[T : JsonEncoder](value: T, result: String): Unit = assertEncode(value, s"\"$result\"")

  val tests = Tests:

    test("encoding"):
      test("statType"):
        test("hp") - assertEncodeStr(StatType.Health, "hp")
        test("atk") - assertEncodeStr(StatType.Attack, "atk")
        test("def") - assertEncodeStr(StatType.Defense, "def")
        test("spa") - assertEncodeStr(StatType.SpecialAttack, "spa")
        test("spd") - assertEncodeStr(StatType.SpecialDefense, "spd")
        test("spe") - assertEncodeStr(StatType.Speed, "spe")

      test("gender"):
        test("male") - assertEncodeStr(Gender.Male, "M")
        test("female") - assertEncodeStr(Gender.Female, "F")

      test("nature"):
        for
          nature <- Nature.values
        do
          assertEncodeStr(nature, nature.toString)

      test("type")
        for
          tpe <- Type.values
        do
          assertEncodeStr(tpe, tpe.toString)

      test("team"):

        val set =
          PokemonSet(
            Some(Surname("Titouan")),
            SpeciesName("Sceptile"),
            Some(Gender.Male),
            None,
            AbilityName("Engrais"),
            Nature.Timid,
            List(MoveName("Leaf Storm"), MoveName("U-turn")).refine,
            Map(
              StatType.Attack -> IV(0)
            ),
            Map(
              StatType.SpecialAttack -> EV(255),
              StatType.Speed -> EV(255)
            ),
            Level(100),
            true,
            Happiness(255),
            "Great Ball",
            None,
            DynamaxLevel(10),
            false,
            Type.Grass
          )

        val result =
          """{"name":"Titouan","species":"Sceptile","gender":"M","item":"","ability":"Engrais","nature":"Timid","moves":["Leaf Storm","U-turn"],"ivs":{"atk":0},"evs":{"spa":255,"spe":255},"level":100,"shiny":true,"happiness":255,"pokeball":"Great Ball","hpType":"","dynamaxLevel":10,"gigantamax":false,"teraType":"Grass"}"""

        assertEncode(set, result)