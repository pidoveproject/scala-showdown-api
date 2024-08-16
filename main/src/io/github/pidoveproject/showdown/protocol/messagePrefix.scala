package io.github.pidoveproject.showdown.protocol

import scala.annotation.StaticAnnotation
import scala.quoted.*

case class messagePrefix(prefix: String) extends StaticAnnotation

object messagePrefix:

  inline def getMessagePrefix[T]: Option[String] = ${ getMessagePrefixImpl[T] }

  def getMessagePrefixImpl[T: Type](using Quotes): Expr[Option[String]] =
    import quotes.reflect.*

    val repr = TypeRepr.of[T]
    val typeSymbol = repr.typeSymbol
    val annotationRepr = TypeRepr.of[messagePrefix]
    val annotationSymbol = annotationRepr.typeSymbol

    typeSymbol.getAnnotation(annotationSymbol).map(_.asExpr) match
      case Some('{ new `messagePrefix`($prefix: String) }) => '{ Some($prefix) }
      case _                                               => '{ None }
