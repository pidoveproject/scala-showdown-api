package io.github.projectpidove.showdown.protocol.server

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.Count
import io.github.projectpidove.showdown.battle.{Weather as WeatherEffect, *, given}
import io.github.projectpidove.showdown.protocol.{MessageDecoder, ProtocolError, messageName, messagePrefix}
import io.github.projectpidove.showdown.room.{ChatContent, given}
import io.github.projectpidove.showdown.team.*

/**
 * A major action on an active battle.
 */
@messagePrefix("-")
enum BattleMinorActionMessage derives MessageDecoder:

  /**
   * Set or clear the active weather.
   *
   * @param weather the active weather or clear if absent
   */
  case Weather(weather: Option[WeatherEffect])

  /**
   * Start a field effect.
   *
   * @param field the started field effect
   */
  case FieldStart(field: FieldEffect)

  /**
   * End a field effect.
   *
   * @param field the ended field effect
   */
  case FieldEnd(field: FieldEffect)

  /**
   * Start a side-dependent field effect.
   *
   * @param side the side of the effect
   * @param field the field effect
   */
  case SideStart(side: PlayerPosition, field: SideFieldEffect)

  /**
   * End a side-dependent field effect.
   *
   * @param side the side of the effect
   * @param field the field effect
   */
  case SideEnd(side: PlayerPosition, field: SideFieldEffect)

  /**
   * Flip side of field effects (aka Court Change).
   */
  case SwapSideConditions
  
  /**
   * An item was revealed.
   *
   * @param pokemon the item holder
   * @param item the revealed item
   * @param effect the optional cause of the reveal
   */
  case Item(pokemon: PokemonId, item: ItemName, effect: Option[Effect])

  /**
   * An item was destroyed.
   *
   * @param pokemon the item holder
   * @param item    the revealed item
   * @param effect  the optional cause of the destruction
   */
  case EndItem(pokemon: PokemonId, item: ItemName, effect: Option[Effect])

  /**
   * An ability was revealed.
   *
   * @param pokemon the ability holder
   * @param ability the revealed ability
   * @param effect the optional cause of the reveal
   */
  case Ability(pokemon: PokemonId, ability: AbilityName, effect: Option[Effect])

  /**
   * An ability was destroyed.
   *
   * @param pokemon the ability holder
   */
  case EndAbility(pokemon: PokemonId)

  /**
   * A pokemon transformed into another species.
   *
   * @param pokemon the transformed pokemon
   * @param target the pokemon to copy
   * @param effect the optional cause of the transformation
   */
  case Transform(pokemon: PokemonId, target: PokemonId, effect: Option[Effect])

  /**
   * A pokemon mega-evolved.
   *
   * @param pokemon the mega-evolving pokemon
   * @param megaStone the mega stone held by the pokemon
   */
  case Mega(pokemon: PokemonId, megaStone: ItemName)

  /**
   * A pokemon ultra-bursted.
   *
   * @param pokemon the ultra-bursting pokemon
   * @param species the new species of the pokemon (e.g Ultra-Necrozma)
   * @param item the item causing the ultra burst
   */
  @messageName("burst") case UltraBurst(pokemon: PokemonId, species: SpeciesName, item: ItemName)

  /**
   * A pokemon used its Z power.
   *
   * @param pokemon the pokemon using its Z power
   */
  case ZPower(pokemon: PokemonId)

  /**
   * A Z move was countered by a protection move.
   *
   * @param pokemon the pokemon who used its Z power
   */
  case ZBroken(pokemon: PokemonId)

  /**
   * An effect was triggered.
   *
   * @param effect the triggered effect
   */
  case Activate(effect: Effect)

  /**
   * A hint on why an event happened (usually not displayed in Pokemon games).
   *
   * @param message the hint content
   */
  case Hint(message: ChatContent)

  /**
   * Center pokemon automatically in Triple Battle when only one pokemon is remaining on each side.
   */
  case Center

  /**
   * A miscellaneous message sent by the simulator.
   */
  case Message(message: ChatContent)


object BattleMinorActionMessage:
      
  given (using weatherDecoder: MessageDecoder[Weather]): MessageDecoder[Option[Weather]] =
    MessageDecoder.word("none").map(_ => None) <> weatherDecoder.map(Some.apply)