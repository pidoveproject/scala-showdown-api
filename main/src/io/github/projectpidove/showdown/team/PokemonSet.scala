package io.github.projectpidove.showdown.team

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.iltotore.iron.zioJson.given
import zio.json.*
import io.github.projectpidove.showdown.json.{someOrEmptyEncoder, someOrEmptyDecoder}

case class PokemonSet(
    name: Option[Surname] = None,
    species: SpeciesName,
    gender: Option[Gender] = None,
    item: Option[ItemName] = None,
    ability: AbilityName,
    nature: Nature,
    moves: MoveNames = List.empty.assume,
    ivs: IVS = Map.empty,
    evs: EVS = Map.empty,
    level: Level = 100,
    shiny: Boolean = false,
    happiness: Happiness = 255,
    pokeball: String = "",
    hpType: Option[Type] = None,
    dynamaxLevel: DynamaxLevel = 10,
    gigantamax: Boolean = false,
    teraType: Type = Type.Normal
) derives JsonDecoder, JsonEncoder
