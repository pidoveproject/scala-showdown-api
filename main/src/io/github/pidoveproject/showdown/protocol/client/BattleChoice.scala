package io.github.pidoveproject.showdown.protocol.client

import io.github.iltotore.iron.*
import io.github.pidoveproject.showdown.battle.*
import io.github.pidoveproject.showdown.protocol.{MessageDecoder, MessageEncoder}
import io.github.pidoveproject.showdown.team.{MoveName, SpeciesName}

/**
 * A battle choice
 */
enum BattleChoice derives MessageEncoder, MessageDecoder:

  /**
   * Switch in a pokemon.
   *
   * @param pokemon the pokemon to switch in
   */
  case Switch(pokemon: SpeciesName | TeamSlot)

  /**
   * Use a move.
   *
   * @param move the move to use
   * @param target the target, if several
   * @param modifier an optional modifier to the move, like mega evolving or z-move
   */
  case Move(move: MoveName | MoveSlot, target: Option[RelativePosition], modifier: Option[MoveModifier])

  /**
   * Do the first legal action.
   */
  case Default

  /**
   * Do nothing.
   */
  case Pass

  /**
   * Cancel selected action
   */
  case Undo

object BattleChoice:

  private given pokemonIdentifierDecoder: MessageDecoder[SpeciesName | TeamSlot] = MessageDecoder.derivedUnion

  private given moveIdentifierDecoder: MessageDecoder[MoveName | MoveSlot] = MessageDecoder.derivedUnion
