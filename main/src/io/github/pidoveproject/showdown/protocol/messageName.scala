package io.github.pidoveproject.showdown.protocol

import scala.annotation.StaticAnnotation
import scala.quoted.*

/**
 * Use an alias for the annotated enum case/case class.
 *
 * @param name the name to use instead
 * @param aliases further aliases
 */
case class messageName(name: String, aliases: String*) extends StaticAnnotation

object messageName:

  /**
   * Get message aliases of a enum case/case class.
   *
   * @tparam T the type of the annotated member
   * @return the aliases of the annotated type
   */
  inline def getMessageNames[T]: Seq[String] = ${ getMessageNamesImpl[T] }

  private def getMessageNamesImpl[T: Type](using Quotes): Expr[Seq[String]] =
    import quotes.reflect.*

    val repr = TypeRepr.of[T]
    val typeSymbol = repr.typeSymbol
    val annotationRepr = TypeRepr.of[messageName]
    val annotationSymbol = annotationRepr.typeSymbol

    typeSymbol.getAnnotation(annotationSymbol).map(_.asExpr) match
      case Some('{ new `messageName`($name: String, $aliases*) }) => '{ $aliases.prepended($name) }
      case _ =>                                                      '{ Seq.empty }
