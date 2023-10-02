package io.github.projectpidove.showdown.battle

import io.github.projectpidove.showdown.team.ItemName

/**
 * The held item of a pokemon.
 */
enum HeldItem:

  /**
   * A revealed held item.
   *
   * @param item the revealed item
   * @param cause the optional cause of the reveal (e.g Frisk)
   */
  case Revealed(item: ItemName, cause: Option[Effect])

  /**
   * A no longer held item.
   *
   * @param item the originally held item
   * @param cause the cause of the item destruction (e.g Knock Off)
   */
  case Destroyed(item: ItemName, cause: Option[Effect])

  /**
   * The held item is not known.
   */
  case Unknown