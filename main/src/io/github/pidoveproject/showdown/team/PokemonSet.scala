package io.github.pidoveproject.showdown.team

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.iltotore.iron.zioJson.given
import zio.json.*
import io.github.pidoveproject.showdown.json.{someOrEmptyEncoder, someOrEmptyDecoder}

/**
 * A pokemon set.
 *
 * @param name this pokemon's surname
 * @param species this pokemon's species
 * @param gender this pokemon's gender or `None` if genderless
 * @param item this pokemon's item or `None`
 * @param ability this pokemon's ability
 * @param nature this pokemon's nature
 * @param moves this pokemon's moves (up to 4)
 * @param ivs this pokemon's internal values (IVs)
 * @param evs this pokemon's effot values (EVs)
 * @param level this pokemon's level
 * @param shiny whether this pokemon is shiny or not
 * @param happiness this pokemon's happiness
 * @param pokeball this pokemon's pokeball. Not used by Showdown but still present
 * @param hpType this pokemon's hidden power type. If `None`, the type is calculated from the IVs
 * @param dynamaxLevel this pokemon's dynamax level (8G only)
 * @param gigantamax whether this pokemon has gigantamax form or not (8G only)
 * @param teraType the type this pokemon takes when terastallizing (9G only)
 */
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
    level: Level = Level(100),
    shiny: Boolean = false,
    happiness: Happiness = Happiness(255),
    pokeball: String = "",
    hpType: Option[Type] = None,
    dynamaxLevel: DynamaxLevel = DynamaxLevel(10),
    gigantamax: Boolean = false,
    teraType: Type = Type.Normal
) derives JsonDecoder, JsonEncoder
