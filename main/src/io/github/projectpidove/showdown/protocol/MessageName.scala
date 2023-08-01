package io.github.projectpidove.showdown.protocol

import scala.annotation.StaticAnnotation
import scala.quoted.*

case class MessageName(name: String, aliases: String*) extends StaticAnnotation

object MessageName:

  inline def getMessageNames[T]: Seq[String] = ${getMessageNamesImpl[T]}

  private def getMessageNamesImpl[T: Type](using Quotes): Expr[Seq[String]] =
    import quotes.reflect.*

    val repr = TypeRepr.of[T]
    val typeSymbol = repr.typeSymbol
    val annotationRepr = TypeRepr.of[MessageName]
    val annotationSymbol = annotationRepr.typeSymbol

    typeSymbol.getAnnotation(annotationSymbol).map(_.asExpr) match
      case Some('{new MessageName($name: String, $aliases: _*)}) => '{$aliases.prepended($name)}
      case _ => '{Seq.empty}