package io.github.projectpidove.showdown.team.pokepaste

import io.github.projectpidove.showdown.team
import io.github.projectpidove.showdown.team.PokemonSet

enum Attribute[+T](val apply: PokemonSet => PokemonSet, val extract: PokemonSet => Option[T]):
  case Surname(value: team.Surname) extends Attribute(_.copy(name = Some(value)), _.name)
  case Species(value: team.SpeciesName) extends Attribute(_.copy(species = value), x => Some(x.species))
  case Gender(value: team.Gender) extends Attribute(_.copy(gender = Some(value)), _.gender)
  case Item(value: team.ItemName) extends Attribute(_.copy(item = Some(value)), _.item)
  case Ability(value: team.AbilityName) extends Attribute(_.copy(ability = value), x => Some(x.ability))
  case Nature(value: team.Nature) extends Attribute(_.copy(nature = value), x => Some(x.nature))
  case Moves(value: team.MoveNames) extends Attribute(_.copy(moves = value), x => Some(x.moves))
  case IVS(value: team.IVS) extends Attribute(_.copy(ivs = value), x => Some(x.ivs))
  case EVS(value: team.EVS) extends Attribute(_.copy(evs = value), x => Some(x.evs))
  case Level(value: team.Level) extends Attribute(_.copy(level = value), x => Some(x.level))
  case Shiny(value: Boolean) extends Attribute(_.copy(shiny = value), x => Some(x.shiny))
  case Happiness(value: team.Happiness) extends Attribute(_.copy(happiness = value), x => Some(x.happiness))
  case TeraType(value: team.Type) extends Attribute(_.copy(teraType = value), x => Some(x.teraType))

  def value: T