package io.github.projectpidove.showdown.protocol

import scala.annotation.StaticAnnotation
import scala.quoted.*

case class MessageName(name: String) extends StaticAnnotation

object MessageName:

  inline def getMessageName[T]: Option[String] = ${getMessageNameImpl[T]}

  private def getMessageNameImpl[T: Type](using Quotes): Expr[Option[String]] =
    import quotes.reflect.*

    val repr = TypeRepr.of[T]
    val typeSymbol = repr.typeSymbol
    val annotationRepr = TypeRepr.of[MessageName]
    val annotationSymbol = annotationRepr.typeSymbol

    typeSymbol.getAnnotation(annotationSymbol).map(_.asExpr) match
      case Some('{new MessageName($name: String)}) => '{Some($name)}
      case _ => '{None}