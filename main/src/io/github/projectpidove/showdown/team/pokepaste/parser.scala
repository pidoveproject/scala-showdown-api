package io.github.projectpidove.showdown.team.pokepaste

import zio.Chunk
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.collection.MaxLength
import io.github.projectpidove.showdown.team.*
import zio.parser.*
import zio.parser.internal.{PUnzippable, PZippable}

import scala.collection.mutable

extension [Err, In, Out, Value](syntax: Syntax[Err, In, Out, Value])

  private inline def refined[C, T](companion: RefinedTypeOpsImpl[Value, C, T])(using
      inline constraint: Constraint[Value, C]
  ): Syntax[Err | String, In, Out, T] =
    syntax.transformEither(
      companion.either(_),
      x => Right(x.asInstanceOf[Value])
    )

  private def ~~[Err2 >: Err | String, In2 <: In & Char, Out2 >: Out | Char, Value2, ZippedValue](that: => Syntax[Err2, In2, Out2, Value2])(using
      PUnzippable.In[Value, Value2, ZippedValue],
      PZippable.Out[Value, Value2, ZippedValue]
  ): Syntax[Err2, In2, Out2, ZippedValue] =
    (syntax <~ whitespaces) ~ that

  private def whitespaced: Syntax[Err | String, In & Char, Out | Char, Value] = syntax.surroundedBy(whitespaces)

  private def parenthesized: Syntax[Err | String, In & Char, Out | Char, Value] = Syntax.char('(') ~> syntax <~ Syntax.char(')')

extension [In, Out, Value](syntax: Syntax[String, In, Out, Value])

  private def withKey(key: String): Syntax[String, In, Out, (String, ?)] = syntax.transformTo[String, (String, Value), Nothing](
    (key, _),
    { case (k, value) if k == key => value},
    s"Wrong key. Expected $key"
  ).asInstanceOf

//  private def toAttribute(attribute: Value => Attribute[Value]): Syntax[Err | String, In, Out, Attribute[?]] = syntax.transform[Attribute[Value]](
//    attribute,
//    _.value
//  ).widenWith("Should not happen")

val whitespaces = Syntax.whitespace.repeat0.unit(Chunk.from(" "))

val newline = Syntax.charIn("\n\r").unit('\n')
val endOfLine = newline | Syntax.end
val lines = Syntax.end | newline.repeat0.autoBacktracking.unit(Chunk.empty)

def nonBlankSyntax[Err, In, Out >: Char](end: Syntax[Err, In, Out, Unit] = Syntax.charIn("\n\r").unit(' ') | Syntax.end) =
  Syntax
    .anyChar
    .repeatUntil(end)
    .transformEither(
      data => Right(data.mkString.trim).filterOrElse(!_.isBlank, "String must not be blank"),
      x => Right(Chunk.from(x))
    )

val intSyntax = Syntax.digit.repeat.transformOption(
  _.mkString.toIntOption,
  x => Some(Chunk.from(x.toString))
).mapError(_.getOrElse("Cannot parse integer"))

val booleanSyntax = Syntax.string("Yes", true) | Syntax.string("yes", true) | Syntax.string("No", false) | Syntax.string("no", false)

val surnameSyntax = nonBlankSyntax(Syntax.end | Syntax.char('(')).refined(Surname)

val abilitySyntax = nonBlankSyntax().refined(AbilityName)
val moveSyntax = nonBlankSyntax().refined(MoveName)
val itemSyntax = nonBlankSyntax().refined(ItemName)
val levelSyntax = intSyntax.refined(Level)
val dynamaxLevelSyntax = intSyntax.refined(DynamaxLevel)
val happinessSyntax = intSyntax.refined(Happiness)
val ivValueSyntax = intSyntax.refined(IV)
val evValueSyntax = intSyntax.refined(EV)
val statTypeSyntax = Syntax.letter.repeat.transformEither(
  data => StatType.fromShortName(data.mkString).toRight(s"Unknown stat type \"$data\""),
  x => Right(Chunk.from(x.shortName))
)

val genderSyntax = Syntax.letter.transformEither(
  data => Gender.fromShortName(data.toString).toRight(s"Unknown gender \"$data\""),
  x => Right(x.shortName.head)
)

val natureSyntax = Syntax.letter.repeat.transformEither(
  data => Nature.values.find(_.toString.equalsIgnoreCase(data.mkString)).toRight(s"Unknown nature \"$data\""),
  x => Right(Chunk.from(x.toString))
)

val typeSyntax = Syntax.letter.repeat.transformEither(
  data => Type.values.find(_.toString.equalsIgnoreCase(data.mkString)).toRight(s"Unknown type \"$data\""),
  x => Right(Chunk.from(x.toString))
)

val ivSyntax = (ivValueSyntax ~~ statTypeSyntax).transform(
  (a, b) => (b, a),
  (a, b) => (b, a)
)
val evSyntax = (evValueSyntax ~~ statTypeSyntax).transform(
  (a, b) => (b, a),
  (a, b) => (b, a)
)

private def parseSpeciesGenderSurname(line: String): Either[String, (SpeciesName, Option[Gender], Option[Surname])] =
  var buffer = line
  val gender =
    if buffer.endsWith("(M)") then
      buffer = buffer.dropRight(4).trim;
      Some(Gender.Male)
    else if buffer.endsWith("(F)") then
      buffer = buffer.dropRight(4).trim;
      Some(Gender.Female)
    else None

  if buffer.endsWith(")") && buffer.contains("(") then
    val arr = buffer.dropRight(1).split("\\(")
    for
      species <- SpeciesName.either(arr.last.trim)
      surname <- Surname.either(arr.dropRight(1).mkString("(").trim)
    yield
      (species, gender, Some(surname))
  else
    for
      species <- SpeciesName.either(buffer.trim)
    yield
      (species, gender, None)

private def printSpeciesGenderSurname(species: SpeciesName, genderOption: Option[Gender], surnameOption: Option[Surname]): String =
  val speciesSurname = surnameOption.fold(species)(surname => s"$surname ($species)")
  genderOption.fold(speciesSurname)(gender => s"$speciesSurname (${gender.shortName})")

def speciesSurnameGenderSyntax[Err, In, Out >: Char](end: Syntax[Err, In, Out, Unit] = Syntax.end) =
  Syntax
    .anyChar
    .repeatUntil(end)
    .transformEither(
      data => parseSpeciesGenderSurname(data.mkString.trim),
      (species, gender, surname) => Right(Chunk.from(printSpeciesGenderSurname(species, gender, surname)))
    )

private val firstLineNoItemSyntax: Syntax[String, Char, Char, ((SpeciesName, Option[Gender], Option[Surname], Option[ItemName]))] =
  speciesSurnameGenderSyntax(endOfLine).transformTo(
    (species, gender, surname) => (species, gender, surname, None),
    { case (species, gender, surname, None) => (species, gender, surname) },
    "Cannot print item"
  )

private val firstLineItemSyntax: Syntax[String, Char, Char, ((SpeciesName, Option[Gender], Option[Surname], Option[ItemName]))] =
  (speciesSurnameGenderSyntax(Syntax.char('@')) ~ itemSyntax).transformTo(
    (species, gender, surname, item) => (species, gender, surname, Some(item)),
    { case (species, gender, surname, Some(item)) => (species, gender, surname, item) },
    "No item"
  )

val firstLineSyntax = firstLineItemSyntax | firstLineNoItemSyntax

val abilityLineSyntax = Syntax.string("Ability:", ()) ~> whitespaces ~ abilitySyntax

val natureLineSyntax = natureSyntax <~ whitespaces <~ Syntax.string("Nature", ())

val moveLineSyntax = Syntax.char('-') ~> whitespaces ~> moveSyntax
val moveListSyntax: Syntax[String, Char, Char, MoveNames] = moveLineSyntax.repeatWithSep(endOfLine).transformEither(
  _.toList.refineEither[MaxLength[4]],
  moves => Right(Chunk.from(moves))
)

val shinyLineSyntax = Syntax.string("Shiny:", ()) ~~ booleanSyntax

val levelLineSyntax = Syntax.string("Level:", ()) ~~ levelSyntax

val teraTypeLineSyntax = Syntax.string("Tera Type", ()) ~~ typeSyntax

val ivsSyntax = Syntax.string("IVs:", ()) ~> whitespaces ~> ivSyntax.repeatWithSep(Syntax.char('/').whitespaced).transform(
  _.toMap,
  Chunk.from
)

val evsSyntax = Syntax.string("EVs:", ()) ~> whitespaces ~> evSyntax.repeatWithSep(Syntax.char('/').whitespaced).transform(
  _.toMap,
  Chunk.from
)

private val abilitySet = abilityLineSyntax.withKey("ability")
private val natureSet = natureLineSyntax.withKey("nature")
private val moveSet = moveListSyntax.withKey("moves")
private val shinySet = shinyLineSyntax.withKey("shiny")
private val levelSet = levelLineSyntax.withKey("level")
private val teraTypeSet = teraTypeLineSyntax.withKey("teraType")
private val ivsSet = ivsSyntax.withKey("ivs")
private val evsSet = evsSyntax.withKey("evs")

private val setSyntax = abilitySet | natureSet | moveSet | shinySet | levelSet | teraTypeSet | ivsSet | evsSet

val pokemonSet = ((firstLineSyntax <~ endOfLine) ~ setSyntax.repeatWithSep0(endOfLine.repeat0.unit(Chunk(())))).transformEither[String, PokemonSet](
  (species, gender, surname, item, attributes) =>
    val map = attributes.toMap
    for
      ability <- map.get("ability").toRight("Missing Ability line").asInstanceOf[Either[String, AbilityName]]
      nature = map.getOrElse("nature", Nature.Serious).asInstanceOf[Nature]
      moves = map.getOrElse("moves", List.empty).asInstanceOf[MoveNames]
      shiny = map.getOrElse("shiny", false).asInstanceOf[Boolean]
      level = map.getOrElse("level", Level(100)).asInstanceOf[Level]
      ivs = map.getOrElse("ivs", Map.empty).asInstanceOf[IVS]
      evs = map.getOrElse("evs", Map.empty).asInstanceOf[EVS]
      teraType = map.getOrElse("teraType", Type.Normal).asInstanceOf[Type]
    yield
      PokemonSet(surname, species, gender, item, ability, nature, moves, ivs, evs, level, shiny, teraType = teraType),
  set =>
    val attributes = Chunk(
      "ability" -> set.ability,
      "nature" -> set.nature,
      "moves" -> set.moves,
      "shiny" -> set.shiny,
      "level" -> set.level,
      "ivs" -> set.ivs,
      "evs" -> set.evs,
      "teraType" -> set.teraType
    )

    Right((set.species, set.gender, set.name, set.item, attributes))
)