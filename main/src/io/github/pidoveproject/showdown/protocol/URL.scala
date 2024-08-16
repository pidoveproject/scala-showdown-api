package io.github.pidoveproject.showdown.protocol

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.string.ValidURL

opaque type URL = String :| ValidURL
object URL extends RefinedTypeOpsImpl[String, ValidURL, URL]
