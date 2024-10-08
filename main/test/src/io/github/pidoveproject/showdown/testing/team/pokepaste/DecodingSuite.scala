package io.github.pidoveproject.showdown.testing.team.pokepaste

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.pidoveproject.showdown.team.*
import io.github.pidoveproject.showdown.team.pokepaste.*
import utest.*
import zio.parser.*

object DecodingSuite extends TestSuite:

  def assertParse[Value](syntax: Syntax[?, Char, ?, Value], input: String, expected: Value): Unit =
    val r = (syntax <~ Syntax.end).parseString(input)
    assert(r == Right(expected))

  def assertFail(syntax: Syntax[?, Char, ?, ?], input: String): Unit =
    assert((syntax <~ Syntax.end).parseString(input).isLeft)

  val tests = Tests:

    test("newline"):
      test - assertParse(newline, "\u000D\u000A", ())
      test - assertParse(newline, "\u000A", ())
      test - assertParse(newline, "\u000B", ())
      test - assertParse(newline, "\u000C", ())
      test - assertParse(newline, "\u000D", ())
      test - assertParse(newline, "\u0085", ())
      test - assertParse(newline, "\u2028", ())
      test - assertParse(newline, "\u2029", ())

    test("nonBlank"):
      test("valid") - assertParse(nonBlankSyntax(), "Artikuno", "Artikuno")
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
      test("multiple") - assertParse(
        ivsSyntax,
        "IVs: 31 HP / 31 Atk / 30 Def",
        Map(
          StatType.Health -> IV(31),
          StatType.Attack -> IV(31),
          StatType.Defense -> IV(30)
        )
      )
      test("missingSep") - assertFail(ivsSyntax, "IVs: 31 HP 31 Atk")

    test("evs"):
      test("single") - assertParse(evsSyntax, "EVs: 31 HP", Map(StatType.Health -> EV(31)))
      test("multiple") - assertParse(
        evsSyntax,
        "EVs: 31 HP / 31 Atk / 30 Def",
        Map(
          StatType.Health -> EV(31),
          StatType.Attack -> EV(31),
          StatType.Defense -> EV(30)
        )
      )
      test("missingSep") - assertFail(evsSyntax, "EVs: 31 HP 31 Atk")

    test("speciesLine"):
      test("species") - assertParse(speciesSurnameGenderSyntax(), "Heracross", (SpeciesName("Heracross"), None, None))
      test("speciesGender") - assertParse(speciesSurnameGenderSyntax(), "Heracross (M)", (SpeciesName("Heracross"), Some(Gender.Male), None))
      test("speciesSurname"):
        test("standard") - assertParse(
          speciesSurnameGenderSyntax(),
          "Heracles (Heracross)",
          (SpeciesName("Heracross"), None, Some(Surname("Heracles")))
        )
        test("withParentheses") - assertParse(
          speciesSurnameGenderSyntax(),
          "Heracles (Hey) (Heracross)",
          (SpeciesName("Heracross"), None, Some(Surname("Heracles (Hey)")))
        )
      test("speciesSurnameGender") - assertParse(
        speciesSurnameGenderSyntax(),
        "Heracles (Heracross) (M)",
        (SpeciesName("Heracross"), Some(Gender.Male), Some(Surname("Heracles")))
      )

    test("firstLine"):
      test("withoutSpace") - assertParse(
        firstLineSyntax,
        "Heracross @ Heracrossite",
        (SpeciesName("Heracross"), None, None, Some(ItemName("Heracrossite")))
      )
      test("withSpace") - assertParse(
        firstLineSyntax,
        "Heracross @ Choice Scarf",
        (SpeciesName("Heracross"), None, None, Some(ItemName("Choice Scarf")))
      )

    test("ability") - assertParse(abilityLineSyntax, "Ability: Hyper Cutter", AbilityName("Hyper Cutter"))

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
          |- Ice Beam""".stripMargin
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
        species = SpeciesName("Articuno"),
        item = Some(ItemName("Leftovers")),
        ability = AbilityName("Pressure"),
        nature = Nature.Modest,
        ivs = Map(StatType.SpecialAttack -> IV(30), StatType.SpecialDefense -> IV(30)),
        evs = Map(StatType.Health -> EV(252), StatType.SpecialAttack -> EV(252), StatType.SpecialDefense -> EV(4)),
        moves = List(MoveName("Ice Beam"), MoveName("Hurricane"), MoveName("Substitute"), MoveName("Roost")).assume
      )

      assertParse(pokemonSet, set, result)

    test("tier"):
      test("valid") - assertParse(tierSyntax, "[gen7monotype]", Tier("gen7monotype"))
      test("missingBrackets") - assertFail(tierSyntax, "gen9ou")

    test("team"):
      val teamSet =
        """=== [gen9ou] Articulo ===
          |
          |Articuno @ Leftovers
          |Ability: Pressure
          |EVs: 252 HP / 252 SpA / 4 SpD
          |Modest Nature
          |IVs: 30 SpA / 30 SpD
          |- Ice Beam
          |- Hurricane
          |- Substitute
          |- Roost
          |
          |Ludicolo @ Life Orb
          |Ability: Swift Swim
          |EVs: 4 HP / 252 SpA / 252 Spe
          |Modest Nature
          |- Surf
          |- Giga Drain
          |- Ice Beam
          |- Rain Dance""".stripMargin

      val result = Team(
        name = TeamName("Articulo"),
        tier = Tier("gen9ou"),
        sets = List(
          PokemonSet(
            species = SpeciesName("Articuno"),
            item = Some(ItemName("Leftovers")),
            ability = AbilityName("Pressure"),
            nature = Nature.Modest,
            ivs = Map(StatType.SpecialAttack -> IV(30), StatType.SpecialDefense -> IV(30)),
            evs = Map(StatType.Health -> EV(252), StatType.SpecialAttack -> EV(252), StatType.SpecialDefense -> EV(4)),
            moves = List(MoveName("Ice Beam"), MoveName("Hurricane"), MoveName("Substitute"), MoveName("Roost")).assume
          ),
          PokemonSet(
            species = SpeciesName("Ludicolo"),
            item = Some(ItemName("Life Orb")),
            ability = AbilityName("Swift Swim"),
            nature = Nature.Modest,
            evs = Map(StatType.Health -> EV(4), StatType.SpecialAttack -> EV(252), StatType.Speed -> EV(252)),
            moves = List(MoveName("Surf"), MoveName("Giga Drain"), MoveName("Ice Beam"), MoveName("Rain Dance")).assume
          )
        ).assume
      )

      assertParse(teamSyntax, teamSet, result)
