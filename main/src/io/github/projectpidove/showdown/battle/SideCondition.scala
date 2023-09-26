package io.github.projectpidove.showdown.battle

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.Count
import scala.math.Integral.Implicits.infixIntegralOps

case class SideCondition(effects: Map[SideFieldEffect, Count]):

  def withEffect(effect: SideFieldEffect): SideCondition =
    this.copy(effects = effects.updated(effect, effects.getOrElse(effect, Count(0))+Count(1)))

  def removedEffect(effect: SideFieldEffect): SideCondition =
    this.copy(effects = effects.removed(effect))

  def cleared: SideCondition = this.copy(effects = Map.empty)