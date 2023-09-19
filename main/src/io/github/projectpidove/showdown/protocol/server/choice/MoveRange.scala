package io.github.projectpidove.showdown.protocol.server.choice

import zio.json.*

/**
 * The range of a move.
 */
@jsonMemberNames(CamelCase)
enum MoveRange: //TODO hit all ennemies & hit everyone?

  /**
   * Target a single opponent.
   */
  case Normal

  /**
   * Target the user's side.
   */
  case AllySide

  /**
   * Target the user.
   */
  case Self

object MoveRange:

  given JsonDecoder[MoveRange] =
    JsonDecoder
      .string
      .mapOrFail(name => MoveRange.values.find(_.toString.equalsIgnoreCase(name)).toRight(s"Invalid move range: $name"))