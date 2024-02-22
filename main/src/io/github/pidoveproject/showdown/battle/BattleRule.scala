package io.github.pidoveproject.showdown.battle

import io.github.pidoveproject.showdown.protocol.{MessageDecoder, ProtocolError}

/**
 * The rule of a battle or format.
 * 
 * @param name the name of this rule
 * @param description a short description of this rule
 */
case class BattleRule(name: String, description: String)

object BattleRule:

  given MessageDecoder[BattleRule] = MessageDecoder.string.mapEither:
    case s"$name: $description" => Right(BattleRule(name, description))
    case value => Left(ProtocolError.InvalidInput(value, "Invalid rule format"))