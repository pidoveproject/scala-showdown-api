package io.github.projectpidove.showdown.team.pokepaste

import io.github.iltotore.iron.*
import zio.Chunk
import zio.parser.*
import zio.parser.internal.{PUnzippable, PZippable}

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

val whitespaces = Syntax.whitespace.repeat0.unit(Chunk.from(" "))

private def lineBreak(print: String) = (Syntax.string("\u000D\u000A", print) | Syntax.charIn("\u000A\u000B\u000C\u000D\u0085\u2028\u2029").string).unit(print)
val newline = lineBreak(System.lineSeparator())
val newlineSpace = lineBreak(" ")
val endOfLine = newline | Syntax.end
val lines = Syntax.end | newline.repeat0.autoBacktracking.unit(Chunk.empty)

def nonBlankSyntax[Err, In, Out >: Char](end: Syntax[Err, In, Out, Unit] = newlineSpace | Syntax.end) =
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