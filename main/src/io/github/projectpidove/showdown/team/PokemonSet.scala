package io.github.projectpidove.showdown.team

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.iltotore.iron.zioJson.given
import zio.json.*
import io.github.projectpidove.showdown.json.{someOrEmptyEncoder, someOrEmptyDecoder}

case class PokemonSet(
    name: Option[Surname],
    species: SpeciesName,
    gender: Option[Gender],
    item: Option[ItemName],
    ability: AbilityName,
    nature: Nature,
    moves: MoveNames,
    ivs: IVS,
    evs: EVS,
    level: Level,
    shiny: Boolean,
    happiness: Happiness,
    pokeball: String,
    hpType: Option[Type],
    dynamaxLevel: DynamaxLevel,
    gigantamax: Boolean,
    teraType: Type
) derives JsonDecoder, JsonEncoder