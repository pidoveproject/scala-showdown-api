package io.github.projectpidove.showdown.testing.team.pokepaste

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.projectpidove.showdown.team.*
import io.github.projectpidove.showdown.team.pokepaste.*
import utest.*
import zio.parser.*

object EncodingSuite extends TestSuite:

  def assertPrint[Value](syntax: Syntax[?, ?, Char, Value], value: Value, expected: String): Unit =
    val r = (syntax <~ Syntax.end).printString(value).map(_.split("\n").map(_.trim).mkString)
    assert(r == Right(expected))

  val tests = Tests:
    test("boolean"):
      test("yes") - assertPrint(booleanSyntax, true, "Yes")
      test("no") - assertPrint(booleanSyntax, false, "No")

    test("gender"):
      test("male") - assertPrint(genderSyntax, Gender.Male, "M")
      test("female") - assertPrint(genderSyntax, Gender.Female, "F")

    test("statType"):
      test("hp") - assertPrint(statTypeSyntax, StatType.Health, "HP")
      test("atk") - assertPrint(statTypeSyntax, StatType.Attack, "Atk")
      test("def") - assertPrint(statTypeSyntax, StatType.Defense, "Def")
      test("spa") - assertPrint(statTypeSyntax, StatType.SpecialAttack, "SpA")
      test("spd") - assertPrint(statTypeSyntax, StatType.SpecialDefense, "SpD")
      test("spe") - assertPrint(statTypeSyntax, StatType.Speed, "Spe")

    test("iv") - assertPrint(ivSyntax, StatType.Health -> IV(31), "31 HP")
    test("ev") - assertPrint(evSyntax, StatType.Health -> EV(255), "255 HP")

    test("ivs"):
      test("single") - assertPrint(ivsSyntax, Map(StatType.Health -> IV(31)), "IVs: 31 HP")
      test("multiple") - assertPrint(ivsSyntax, Map(StatType.Health -> IV(31), StatType.Attack -> IV(31)), "IVs: 31 HP / 31 Atk")

    test("evs"):
      test("single") - assertPrint(evsSyntax, Map(StatType.Health -> EV(255)), "EVs: 255 HP")
      test("multiple") - assertPrint(evsSyntax, Map(StatType.Health -> EV(255), StatType.Attack -> EV(255)), "EVs: 255 HP / 255 Atk")

    test("ability") - assertPrint(abilityLineSyntax, AbilityName("Hyper Cutter"), "Ability: Hyper Cutter")

    test("nature") - assertPrint(natureLineSyntax, Nature.Adamant, "Adamant Nature")

    test("move"):
      test("single") - assertPrint(moveLineSyntax, MoveName("Hydro Pump"), "- Hydro Pump")
      test("multiple") - assertPrint(
        moveListSyntax,
        List(
          MoveName("Hydro Pump"),
          MoveName("Surf"),
          MoveName("Shadow Ball"),
          MoveName("Ice Beam")
        ).assume,
        """- Hydro Pump
          |- Surf
          |- Shadow Ball
          |- Ice Beam""".stripMargin.split("\n").map(_.trim).mkString
      )