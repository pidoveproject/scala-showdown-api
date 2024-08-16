package io.github.pidoveproject.showdown.protocol

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.any.Not
import io.github.iltotore.iron.constraint.string.Blank

private type AssertionConstraint = Not[Blank]
opaque type Assertion <: String = String :| AssertionConstraint
object Assertion extends RefinedTypeOpsImpl[String, AssertionConstraint, Assertion]
