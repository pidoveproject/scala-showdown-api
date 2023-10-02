package io.github.projectpidove.showdown.battle

import io.github.projectpidove.showdown.protocol.MessageEncoder

/**
 * The relative position of an active pokemon.
 * 
 * @param side the relative side of the pokemon
 * @param slot the slot of the pokemon
 */
case class RelativePosition(side: RelativeSide, slot: PokemonSlot)

object RelativePosition:

  given MessageEncoder[RelativePosition] = MessageEncoder.string.contramap: target =>
    val side =
      if target.side == RelativeSide.Ally then "-"
      else "+"

    s"$side${target.slot}"
