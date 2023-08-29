package io.github.projectpidove.showdown.protocol.client

import io.github.projectpidove.showdown.{FormatName, Generation}
import io.github.projectpidove.showdown.protocol.MessageEncoder
import io.github.projectpidove.showdown.team.{SpeciesName, Tier}

/**
 * A command giving information about the game.
 */
enum InformationCommand derives MessageEncoder:

  /**
   * Show signification of group rank.
   *
   * @param target the group type to check, either global, room or both if absent
   */
  case Groups(target: Option[GroupTarget])

  /**
   * Get FAQ answers.
   *
   * @param topic the paragraph/theme to show
   */
  case Faq(topic: Option[String])

  /**
   * Set the rules of the current room.
   *
   * @param url the new url for this room's rules
   */
  case Rules(url: Option[String])

  /**
   * Show introduction message.
   */
  case Intro

  /**
   * Show help links about a format.
   *
   * @param format the format to get help about or global help if absent
   */
  case FormatsHelp(format: Option[FormatName])

  /**
   * Get the link to Other Metagames Smogon page.
   */
  case OtherMetas

  /**
   * Get the link to Smogon University's analysis of a pokemon.
   *
   * @param pokemon the species to analyze
   * @param generation the generation of the analysis or latest if absent
   * @param tier the tier of the analysis or the species' default if absent
   */
  case Analysis(pokemon: SpeciesName, generation: Option[Generation], tier: Option[Tier])

  /**
   * Get explanation on punishments.
   */
  case Punishments

  /**
   * Get the link to Pokemon Showdown's calculator.
   */
  case Calc

  /**
   * Get the link to Pokemon Showdown's Random Battle calculator.
   */
  case RCalc

  /**
   * Get the link to Pokemon Showdown's BSS calculator.
   */
  case BsCalc

  /**
   * Get links to Pokemon Showdown's Git repositories.
   */
  case Git

  /**
   * Show introduction to Create-a-Pokemon.
   */
  case Cap

  /**
   * Get help for room-specific commands.
   */
  case RoomHelp

  /**
   * Get the current room's FAQ answers.
   *
   * @param topic the paragraph/theme to show
   */
  case RoomFaq(topic: Option[String])
