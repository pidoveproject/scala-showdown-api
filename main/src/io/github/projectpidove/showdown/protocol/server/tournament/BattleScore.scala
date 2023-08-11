package io.github.projectpidove.showdown.protocol.server.tournament

import io.github.projectpidove.showdown.protocol.{MessageDecoder, ProtocolError}
import io.github.projectpidove.showdown.protocol.MessageDecoder.*

case class BattleScore(user1: Score, user2: Score)

object BattleScore:

  given MessageDecoder[BattleScore] =
    MessageDecoder
      .string
      .map(_.split(","))
      .mapEither {
        case Array(str1, str2) =>
          for
            score1 <- str1.toIntOption.toRight(ProtocolError.InvalidInput(str1, "Invalid int"))
            score2 <- str2.toIntOption.toRight(ProtocolError.InvalidInput(str2, "Invalid int"))
            user1 <- Score.either(score1).toInvalidInput(str1)
            user2 <- Score.either(score2).toInvalidInput(str2)
          yield BattleScore(user1, user2)

        case array => Left(ProtocolError.InvalidInput(array.mkString(","), "Invalid battle score"))
      }
