package io.github.pidoveproject.showdown.battle

/**
 * A player side relative to another.
 */
enum RelativeSide:

  /**
   * The same side.
   */
  case Ally

  /**
   * The opposite side.
   *
   * @note some formats like Free-for-All have multiple opposite sides
   */
  case Enemy
