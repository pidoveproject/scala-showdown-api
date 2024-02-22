package io.github.pidoveproject.showdown.team

/**
 * A pokemon team set, like in Showdown's team builder.
 *
 * @param name the name of the team
 * @param tier the tier this team is made for
 * @param sets the pokemon sets of this team.
 */
case class Team(name: TeamName, tier: Tier, sets: PokemonSets)
