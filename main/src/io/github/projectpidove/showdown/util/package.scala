package io.github.projectpidove.showdown.util

import io.github.iltotore.iron.*
import scala.compiletime.summonInline
import scala.reflect.TypeTest

inline given newtypeLeft[A, B](using mirror: RefinedTypeOps.Mirror[A]): TypeTest[A, B] =
  summonInline[TypeTest[mirror.IronType, B]].asInstanceOf

inline given newtypeRight[A, B](using mirror: RefinedTypeOps.Mirror[B]): TypeTest[A, B] =
  summonInline[TypeTest[A, mirror.IronType]].asInstanceOf

inline given ironTypeRight[A, B, C](using constraint: Constraint[B, C]): TypeTest[A, B :| C] =
  val test = summonInline[TypeTest[A, B]]

  new TypeTest:
    override def unapply(value: A): Option[value.type & B :| C] =
      test.unapply(value).filter(constraint.test(_)).asInstanceOf