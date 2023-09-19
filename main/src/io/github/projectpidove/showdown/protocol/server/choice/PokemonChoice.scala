package io.github.projectpidove.showdown.protocol.server.choice

import io.github.iltotore.iron.zioJson.given
import io.github.projectpidove.showdown.json.someOrEmptyDecoder
import io.github.projectpidove.showdown.battle.*
import io.github.projectpidove.showdown.team.*
import zio.json.*

/**
 * Information about a selectable pokemon.
 *
 * @param id the team id of the pokemon
 * @param details the details of the pokemon
 * @param condition the health and status of the pokemon
 * @param active `true` if this pokemon is on the terrain, `false` otherwise
 * @param stats the stats of the pokemon
 * @param moves the moves of the pokemon
 * @param item the item held by the pokemon
 * @param pokeball the pokeball of the pokemon, not used
 * @param baseAbility the base ability of the pokemon
 * @param ability the current ability of the pokemon in case of ability change (e.g Mummy, Wandering Spirit, Neutralising Gaz...)
 * @param teraType the tera type of the pokemon
 * @param terastallized the type thep pokemon terastallized into
 */
case class PokemonChoice(
    @jsonField("ident") id: TeamPosition,
    details: PokemonDetails,
    condition: HealthStatus,
    active: Boolean,
    stats: Map[StatType, Stat],
    moves: MoveNames,
    item: ItemName,
    pokeball: String,
    baseAbility: AbilityName,
    ability: AbilityName,
    teraType: Option[Type] = None,
    terastallized: Option[Type] = None
) derives JsonDecoder