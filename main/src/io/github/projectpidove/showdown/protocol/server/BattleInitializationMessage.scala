package io.github.projectpidove.showdown.protocol.server

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.{Count, FormatName, Generation, Timestamp as TimestampValue}
import io.github.projectpidove.showdown.battle.{*, given}
import io.github.projectpidove.showdown.protocol.{MessageDecoder, messageName}
import io.github.projectpidove.showdown.room.ChatContent
import io.github.projectpidove.showdown.team.ItemName
import io.github.projectpidove.showdown.user.{AvatarName, User, Username}

enum BattleInitializationMessage derives MessageDecoder:
  case Player(number: PlayerNumber, name: Username, avatar: AvatarName, rating: Rating)
  case TeamSize(player: PlayerNumber, size: Count)
  case GameType(battleType: BattleType)
  case Gen(generation: Generation)
  case Tier(format: FormatName)
  case Rated(message: Option[ChatContent])
  case Rule(rule: BattleRule)
  @messageName("clearpoke") case StartPreview()
  @messageName("poke") case DeclarePokemon(player: PlayerNumber, details: PokemonDetails, item: ItemName)
  case Start()