package io.github.pidoveproject.showdown.util

import io.github.iltotore.iron.RefinedTypeOps

trait NumericTypeOps[A, C, T](using integral: Integral[A]) extends RefinedTypeOps[A, C, T]:

  given Integral[T] = integral.asInstanceOf[Integral[T]]
