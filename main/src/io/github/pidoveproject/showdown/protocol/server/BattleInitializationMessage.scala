package io.github.pidoveproject.showdown.protocol.server

import io.github.iltotore.iron.*
import io.github.pidoveproject.showdown.{Count, FormatName, Generation}
import io.github.pidoveproject.showdown.battle.{*, given}
import io.github.pidoveproject.showdown.protocol.{MessageDecoder, messageName}
import io.github.pidoveproject.showdown.room.ChatContent
import io.github.pidoveproject.showdown.team.ItemName
import io.github.pidoveproject.showdown.user.{AvatarName, User, Username}

/**
 * A message relative to the initialization of a battle.
 */
enum BattleInitializationMessage derives MessageDecoder:

  /**
   * Declare a battle player
   *
   * @param number the number attributed to the player
   * @param name the name of the user
   * @param avatar the avatar of the user
   * @param rating the rating of the user in the battle's format
   */
  case Player(number: PlayerNumber, name: Option[Username], avatar: Option[AvatarName], rating: Option[Rating])

  /**
   * Declare the size of a player's team.
   *
   * @param player the team owner
   * @param size the number of pokemon in the team
   */
  case TeamSize(player: PlayerNumber, size: Count)

  /**
   * Set the type of the battle.
   *
   * @param battleType the type of the battle
   */
  case GameType(battleType: BattleType)

  /**
   * Set the generation of the battle.
   *
   * @param generation the generation of the battle's format
   */
  case Gen(generation: Generation)

  /**
   * Set the tier of the battle.
   *
   * @param format the format of the battle
   */
  case Tier(format: FormatName)

  /**
   * Set the battle as rated.
   *
   * @param message `None` if the battle is rated or `Some` if the battle is rated in some other way.
   */
  case Rated(message: Option[ChatContent])

  /**
   * Declare a rule of the battle
   *
   * @param rule the enabled battle rule
   */
  case Rule(rule: BattleRule)

  /**
   * Start the team preview.
   */
  @messageName("clearpoke") case StartPreview()

  /**
   * Declare a pokemon.
   *
   * @param player the pokemon's trainer
   * @param details the details of the pokemon
   * @param item the item held by the declared pokemon
   */
  @messageName("poke") case DeclarePokemon(player: PlayerNumber, details: PokemonDetails, item: Option[ItemName])

  /**
   * Start the battle
   */
  case Start()
