package io.github.projectpidove.showdown.team

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
)