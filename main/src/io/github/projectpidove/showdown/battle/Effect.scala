package io.github.projectpidove.showdown.battle

import io.github.projectpidove.showdown.protocol.{MessageDecoder, ProtocolError}
import io.github.projectpidove.showdown.protocol.MessageDecoder.toInvalidInput
import io.github.projectpidove.showdown.team.{AbilityName, ItemName, MoveName}

enum Effect:
  case Ability(ability: AbilityName)
  case HeldItem
  case Item(item: ItemName)
  case Move(move: MoveName)
  case Silent
  case ZEffect(move: MoveName)

  case Miscellaneous(effect: String)
  case Of(effect: Effect, owner: ActiveId)

object Effect:

  private def parse(content: String): Either[ProtocolError, Effect] = content match
    case s"ability: $ability" => AbilityName.either(ability).toInvalidInput(ability).map(Effect.Ability.apply)
    case s"item: $item"       => ItemName.either(item).toInvalidInput(item).map(Effect.Item.apply)
    case s"move: $move"       => MoveName.either(move).toInvalidInput(move).map(Effect.Move.apply)
    case s"zeffect: $move"    => MoveName.either(move).toInvalidInput(move).map(Effect.ZEffect.apply)
    case misc                 => Right(Effect.Miscellaneous(misc))

  def fromString(value: String): Either[ProtocolError, Effect] = value match
    case s"[from] $content" => parse(content)
    case "[fromitem]"       => Right(Effect.HeldItem)
    case "[silent]"         => Right(Effect.Silent)
    case s"$content|[of] $owner" =>
      for
        effect <- parse(content)
        ownerId <- ActiveId.fromString(owner)
      yield Effect.Of(effect, ownerId)

    case _ => parse(value)

  given MessageDecoder[Effect] = MessageDecoder.string.mapEither(fromString)
