package io.github.projectpidove.showdown

case class PokemonSet(
    name: Option[String],
    species: String,
    gender: Option[Gender],
    item: Option[String],
    ability: String,
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
)
