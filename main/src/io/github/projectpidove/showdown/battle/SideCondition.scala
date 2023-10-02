package io.github.projectpidove.showdown.battle

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.Count
import scala.math.Integral.Implicits.infixIntegralOps

/**
 * The condition of a side.
 *
 * @param effects the effects bound to the represented side
 */
case class SideCondition(effects: Map[SideFieldEffect, Count]):

  /**
   * Add an effect to the the side.
   *
   * @param effect the effect to add
   * @return a copy of the side's condition with the given effect
   */
  def withEffect(effect: SideFieldEffect): SideCondition =
    this.copy(effects = effects.updated(effect, effects.getOrElse(effect, Count(0))+Count(1)))

  /**
   * Remove an effect to the the side.
   *
   * @param effect the effect to remove
   * @return a copy of the side's condition without the given effect
   */
  def removedEffect(effect: SideFieldEffect): SideCondition =
    this.copy(effects = effects.removed(effect))

  /**
   * Clear all the effects of the represented side.
   */
  def cleared: SideCondition = this.copy(effects = Map.empty)