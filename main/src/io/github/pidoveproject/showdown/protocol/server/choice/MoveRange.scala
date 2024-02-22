package io.github.pidoveproject.showdown.protocol.server.choice

import zio.json.*

/**
 * The range of a move.
 */
@jsonMemberNames(CamelCase)
enum MoveRange:

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

  /**
   * Target everyone.
   */
  case All

  /**
   * Target all adjacent pokemon.
   */
  case AllAdjacent

  /**
   * Target all adjacent opponents.
   */
  case AllAdjacentFoes

  /**
   * Target all opponents.
   */
  case FoeSide

  /**
   * Random normal.
   */
  case RandomNormal

  /**
   * Target any pokemon.
   */
  case Any

object MoveRange:

  given JsonDecoder[MoveRange] =
    JsonDecoder
      .string
      .mapOrFail(name => MoveRange.values.find(_.toString.equalsIgnoreCase(name)).toRight(s"Invalid move range: $name"))