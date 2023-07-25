package io.github.projectpidove.showdown.testing.team

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.projectpidove.showdown.team.*
import io.github.projectpidove.showdown.team.pokepaste.*
import utest.*
import zio.parser.*

object PokePasteSuite extends TestSuite:

  def assertParse[Value](syntax: Syntax[?, Char, ?, Value], input: String, expected: Value): Unit =
    assert((syntax <~ Syntax.end).parseString(input) == Right(expected))

  def assertFail(syntax: Syntax[?, Char, ?, ?], input: String): Unit =
    assert((syntax <~ Syntax.end).parseString(input).isLeft)

  val tests = Tests:

    test("encoding"):

      test("nonBlank"):
        test("valid") - assertParse(nonBlankSyntax(), "Artikuno", SpeciesName("Artikuno"))
        test("empty") - assertFail(nonBlankSyntax(), "")
        test("blank") - assertFail(nonBlankSyntax(), " ")

      test("int"):
        test("valid") - assertParse(intSyntax, "123", 123)
        test("non-int") - assertFail(intSyntax, "1.0")
        test("nan") - assertFail(intSyntax, "abc")
        test("empty") - assertFail(intSyntax, "")

      test("boolean"):
        test("yes"):
          test - assertParse(booleanSyntax, "Yes", true)
          test - assertParse(booleanSyntax, "yes", true)
        test("no"):
          test - assertParse(booleanSyntax, "No", false)
          test - assertParse(booleanSyntax, "no", false)

      test("gender"):
        test("male") - assertParse(genderSyntax, "M", Gender.Male)
        test("female") - assertParse(genderSyntax, "F", Gender.Female)

      test("statType"):
        test("hp") - assertParse(statTypeSyntax, "HP", StatType.Health)
        test("atk") - assertParse(statTypeSyntax, "Atk", StatType.Attack)
        test("def") - assertParse(statTypeSyntax, "Def", StatType.Defense)
        test("spa") - assertParse(statTypeSyntax, "SpA", StatType.SpecialAttack)
        test("spd") - assertParse(statTypeSyntax, "SpD", StatType.SpecialDefense)
        test("spe") - assertParse(statTypeSyntax, "Spe", StatType.Speed)
        test("invalid") - assertFail(statTypeSyntax, "SpZ")

      test("iv"):
        test("valid") - assertParse(ivSyntax, "31 HP", (StatType.Health, IV(31)))
        test("whitespaced") - assertParse(ivSyntax, "31     HP", (StatType.Health, IV(31)))
        test("invalidStat") - assertFail(ivSyntax, "31 SpZ")
        test("invalidValue") - assertFail(ivSyntax, "40 HP")

      test("ev"):
        test("valid") - assertParse(evSyntax, "31 HP", (StatType.Health, EV(31)))
        test("whitespaced") - assertParse(evSyntax, "31     HP", (StatType.Health, EV(31)))
        test("invalidStat") - assertFail(evSyntax, "31 SpZ")
        test("invalidValue") - assertFail(evSyntax, "260 HP")

      test("ivs"):
        test("single") - assertParse(ivsSyntax, "IVs: 31 HP", Map(StatType.Health -> IV(31)))
        test("multiple") - assertParse(ivsSyntax, "IVs: 31 HP / 31 Atk / 30 Def", Map(
          StatType.Health -> IV(31),
          StatType.Attack -> IV(31),
          StatType.Defense -> IV(30)
        ))
        test("missingSep") - assertFail(ivsSyntax, "IVs: 31 HP 31 Atk")

      test("evs"):
        test("single") - assertParse(evsSyntax, "EVs: 31 HP", Map(StatType.Health -> EV(31)))
        test("multiple") - assertParse(evsSyntax, "EVs: 31 HP / 31 Atk / 30 Def", Map(
          StatType.Health -> EV(31),
          StatType.Attack -> EV(31),
          StatType.Defense -> EV(30)
        ))
        test("missingSep") - assertFail(evsSyntax, "EVs: 31 HP 31 Atk")

      test("speciesLine"):
        test("species") - assertParse(speciesSurnameGenderSyntax(), "Heracross", (SpeciesName("Heracross"), None, None))
        test("speciesGender") - assertParse(speciesSurnameGenderSyntax(), "Heracross (M)", (SpeciesName("Heracross"), Some(Gender.Male), None))
        test("speciesSurname"):
          test("standard") - assertParse(speciesSurnameGenderSyntax(), "Heracles (Heracross)", (SpeciesName("Heracross"), None, Some(Surname("Heracles"))))
          test("withParentheses") - assertParse(speciesSurnameGenderSyntax(), "Heracles (Hey) (Heracross)", (SpeciesName("Heracross"), None, Some(Surname("Heracles (Hey)"))))
        test("speciesSurnameGender") - assertParse(speciesSurnameGenderSyntax(), "Heracles (Heracross) (M)", (SpeciesName("Heracross"), Some(Gender.Male), Some(Surname("Heracles"))))

      test("firstLine"):
        test("withoutSpace") - assertParse(firstLineSyntax, "Heracross @ Heracrossite", (SpeciesName("Heracross"), None, None, Some(ItemName("Heracrossite"))))
        test("withSpace") - assertParse(firstLineSyntax, "Heracross @ Choice Scarf", (SpeciesName("Heracross"), None, None, Some(ItemName("Choice Scarf"))))

      test("ability") - assertParse(abilityLineSyntax, "Ability: Hyper Cutter", MoveName("Hyper Cutter"))

      test("nature"):
        test("valid") - assertParse(natureLineSyntax, "Adamant Nature", Nature.Adamant)
        test("missingSuffix") - assertFail(natureLineSyntax, "Adamant")
        test("invalidValue") - assertFail(natureLineSyntax, "UwU Nature")

      test("move"):
        test("single") - assertParse(moveLineSyntax, "- Hydro Pump", MoveName("Hydro Pump"))
        test("multiple") - assertParse(
          moveListSyntax,
          """- Hydro Pump
            |- Surf
            |- Shadow Ball
            |- Ice Beam""".stripMargin,
          List(
            MoveName("Hydro Pump"),
            MoveName("Surf"),
            MoveName("Shadow Ball"),
            MoveName("Ice Beam")
          ).assume
        )

        test("tooMuch") - assertFail(
          moveListSyntax,
          """- Hydro Pump
            |- Surf
            |- Shadow Ball
            |- Aqua Jet
            |- Ice Beam""".stripMargin,
        )
        
    test("pokemon"):
      val set =
        """Articuno @ Leftovers
          |Ability: Pressure
          |EVs: 252 HP / 252 SpA / 4 SpD
          |Modest Nature
          |IVs: 30 SpA / 30 SpD
          |- Ice Beam
          |- Hurricane
          |- Substitute
          |- Roost""".stripMargin

      val result = PokemonSet(
        species = "Articuno",
        item = Some(ItemName("Leftovers")),
        ability = AbilityName("Pressure"),
        nature = Nature.Modest,
        ivs = Map(StatType.SpecialAttack -> IV(30), StatType.SpecialDefense -> IV(30)),
        evs = Map(StatType.Health -> EV(252), StatType.SpecialAttack -> EV(252), StatType.SpecialDefense -> EV(4)),
        moves = List(MoveName("Ice Beam"), MoveName("Hurricane"), MoveName("Substitute"), MoveName("Roost")).assume
      )

      assertParse(pokemonSet, set, result)