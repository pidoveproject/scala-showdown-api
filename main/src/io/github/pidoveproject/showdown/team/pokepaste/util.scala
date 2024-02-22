package io.github.pidoveproject.showdown.team.pokepaste

import io.github.iltotore.iron.*
import zio.Chunk
import zio.parser.*
import zio.parser.internal.{PUnzippable, PZippable}

extension [Err, In, Out, Value](syntax: Syntax[Err, In, Out, Value])

  /**
   * Refine this syntax using the given RefinedTypeOps.
   *
   * @param companion the companion object of the refined type
   * @tparam C the constraint type
   * @tparam T the refined type
   * @return a new syntax converting `In` to `T`
   */
  private inline def refined[C, T](companion: RefinedTypeOpsImpl[Value, C, T])(using
      inline constraint: Constraint[Value, C]
  ): Syntax[Err | String, In, Out, T] =
    syntax.transformEither(
      companion.either(_),
      x => Right(x.asInstanceOf[Value])
    )

  /**
   * Like `~` but with eventual whitespaces between.
   */
  private def ~~[Err2 >: Err | String, In2 <: In & Char, Out2 >: Out | Char, Value2, ZippedValue](that: => Syntax[Err2, In2, Out2, Value2])(using
      PUnzippable.In[Value, Value2, ZippedValue],
      PZippable.Out[Value, Value2, ZippedValue]
  ): Syntax[Err2, In2, Out2, ZippedValue] =
    (syntax <~ whitespaces) ~ that

  /**
   * Surround this syntax with whitespaces.
   *
   * @return a new syntax supporting prefixed and suffixed whitespaces
   */
  private def whitespaced: Syntax[Err | String, In & Char, Out | Char, Value] = syntax.surroundedBy(whitespaces)

  /**
   * Surround this syntax with parentheses.
   *
   * @return a new syntax requiring parentheses before and after
   */
  private def parenthesized: Syntax[Err | String, In & Char, Out | Char, Value] = Syntax.char('(') ~> syntax <~ Syntax.char(')')

extension [In, Out, Value](syntax: Syntax[String, In, Out, Value])
  /**
   * Annotate this syntax with a key.
   *
   * @param key the key to attach to this syntax
   * @return a new syntax with the same result and the passed key, as a tuple
   */
  private def withKey(key: String): Syntax[String, In, Out, (String, ?)] = syntax.transformTo[String, (String, Value), Nothing](
    (key, _),
    { case (k, value) if k == key => value },
    s"Wrong key. Expected $key"
  ).asInstanceOf

/**
 * A syntax for multiple (or zero) whitespaces.
 */
val whitespaces = Syntax.whitespace.repeat0.unit(Chunk.from(" "))

/**
 * A syntax for line breaks.
 *
 * @param print the text to display when printing
 * @return a new syntax supporting line breaks
 */
private def lineBreak(print: String) =
  (Syntax.string("\u000D\u000A", print) | Syntax.charIn("\u000A\u000B\u000C\u000D\u0085\u2028\u2029").string).unit(print)
val newline = lineBreak(System.lineSeparator())
val newlineSpace = lineBreak(" ")
val endOfLine = newline | Syntax.end
val lines = Syntax.end | newline.repeat0.autoBacktracking.unit(Chunk.empty)

/**
 * A syntax parsing a non-blank String.
 *
 * @param end the syntax marking the end.
 * @return a new syntax parsing a non blank String until `end`
 */
def nonBlankSyntax[Err, In, Out >: Char](end: Syntax[Err, In, Out, Unit] = newlineSpace | Syntax.end) =
  Syntax
    .anyChar
    .repeatUntil(end)
    .transformEither(
      data => Right(data.mkString.trim).filterOrElse(!_.isBlank, "String must not be blank"),
      x => Right(Chunk.from(x))
    )

/**
 * A syntax parsing an integer.
 */
val intSyntax = Syntax.digit.repeat.transformOption(
  _.mkString.toIntOption,
  x => Some(Chunk.from(x.toString))
).mapError(_.getOrElse("Cannot parse integer"))

/**
 * A syntax parsing "yes" or "no" to a boolean.
 */
val booleanSyntax = Syntax.string("Yes", true) | Syntax.string("yes", true) | Syntax.string("No", false) | Syntax.string("no", false)
