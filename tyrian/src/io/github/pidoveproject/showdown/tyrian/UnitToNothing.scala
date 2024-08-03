package io.github.pidoveproject.showdown.tyrian

/**
 * Convert [[Unit]] to [[Nothing]] or return the type as is.
 *
 * @tparam A the type to convert. Left as is if [[A]] is not [[Unit]].
 */
type UnitToNothing[A] = A match
  case Unit => Nothing
  case _    => A