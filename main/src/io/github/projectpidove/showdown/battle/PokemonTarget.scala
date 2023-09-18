package io.github.projectpidove.showdown.battle

import io.github.projectpidove.showdown.protocol.MessageEncoder

case class PokemonTarget(side: PokemonSide, slot: PokemonSlot)

object PokemonTarget:

  given MessageEncoder[PokemonTarget] = MessageEncoder.string.contramap: target =>
    val side =
      if target.side == PokemonSide.Ally then "-"
      else "+"

    s"$side${target.slot}"
