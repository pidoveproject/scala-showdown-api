package io.github.projectpidove.showdown.battle

import io.github.projectpidove.showdown.protocol.MessageEncoder

/**
 * The relative position of an active pokemon.
 * 
 * @param side the relative side of the pokemon
 * @param slot the slot of the pokemon
 */
case class PokemonTarget(side: PokemonSide, slot: PokemonSlot)

object PokemonTarget:

  given MessageEncoder[PokemonTarget] = MessageEncoder.string.contramap: target =>
    val side =
      if target.side == PokemonSide.Ally then "-"
      else "+"

    s"$side${target.slot}"
