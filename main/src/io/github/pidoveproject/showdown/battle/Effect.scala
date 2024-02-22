package io.github.pidoveproject.showdown.battle

import io.github.pidoveproject.showdown.protocol.{MessageDecoder, ProtocolError}
import io.github.pidoveproject.showdown.protocol.MessageDecoder.toInvalidInput
import io.github.pidoveproject.showdown.team.{AbilityName, ItemName, MoveName}

/**
 * An instant effect, usually a cause of change in the battle state.
 */
enum Effect:

  /**
   * An effect caused by an ability.
   *
   * @param ability the causing ability
   */
  case Ability(ability: AbilityName)

  /**
   * An effect caused by the pokemon's currently held item
   */
  case HeldItem

  /**
   * An effect caused by an item.
   *
   * @param item the causing item
   */
  case Item(item: ItemName)

  /**
   * An effect caused of a move.
   *
   * @param move the causing move
   */
  case Move(move: MoveName)

  /**
   * Silence caused by a ability or a move (like Throat Chop).
   */
  case Silent

  /**
   * A Z-move animation/effect.
   *
   * @param move the boosted move
   */
  case ZEffect(move: MoveName)

  /**
   * A unknown/miscellaneous move.
   *
   * @param effect the raw effect as text
   */
  case Miscellaneous(effect: String)

  /**
   * An effect caused by a pokemon.
   *
   * @param effect the triggered effect
   * @param owner the active pokemon causing the effect
   */
  case Of(effect: Effect, owner: ActiveId)

object Effect:

  private def parse(content: String): Either[ProtocolError, Effect] = content match
    case s"ability: $ability" => AbilityName.either(ability).toInvalidInput(ability).map(Effect.Ability.apply)
    case s"item: $item"       => ItemName.either(item).toInvalidInput(item).map(Effect.Item.apply)
    case s"move: $move"       => MoveName.either(move).toInvalidInput(move).map(Effect.Move.apply)
    case s"zeffect: $move"    => MoveName.either(move).toInvalidInput(move).map(Effect.ZEffect.apply)
    case misc                 => Right(Effect.Miscellaneous(misc))

  /**
   * Parse an effect from the given [[String]].
   *
   * @param value the text to parse
   * @return the read effect or a [[ProtocolError]] if it failed
   */
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
