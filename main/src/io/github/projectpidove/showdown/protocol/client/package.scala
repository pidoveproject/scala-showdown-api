package io.github.projectpidove.showdown.protocol.client

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

opaque type Modifier = String :| Not[Blank]
object Modifier extends RefinedTypeOpsImpl[String, Not[Blank], Modifier]