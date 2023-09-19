package io.github.projectpidove.showdown.protocol.client

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.battle.*
import io.github.projectpidove.showdown.protocol.MessageEncoder
import io.github.projectpidove.showdown.team.{MoveName, SpeciesName}

/**
 * A battle choice
 */
enum BattleChoice derives MessageEncoder:

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
  case Move(move: MoveName | MoveSlot, target: Option[PokemonTarget], modifier: Option[MoveModifier])

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
