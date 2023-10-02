package io.github.projectpidove.showdown.util

import io.github.iltotore.iron.RefinedTypeOpsImpl

trait NumericTypeOps[A, C, T](using integral: Integral[A]) extends RefinedTypeOpsImpl[A, C, T]:

  given Integral[T] = integral.asInstanceOf[Integral[T]]



