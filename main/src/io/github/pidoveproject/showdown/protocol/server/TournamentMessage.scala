package io.github.pidoveproject.showdown.protocol.server

import io.github.pidoveproject.showdown.protocol.server.tournament.*
import io.github.pidoveproject.showdown.protocol.{MessageDecoder, messageName}
import io.github.pidoveproject.showdown.room.RoomId
import io.github.pidoveproject.showdown.user.{User, Username}
import io.github.pidoveproject.showdown.{Count, FormatName}

/**
 * A message bound to a tournament
 */
enum TournamentMessage:

  /**
   * A new tournament has been created.
   *
   * @param format the format of the battles of this tournament
   * @param generator the generator of the tournament. Either elimination tree or round robin
   */
  case Create(format: FormatName, generator: TournamentGenerator, playerCap: Count)

  /**
   * An update on the current tournament.
   *
   * @param data the updated data of the tournament
   */
  case Update(data: TournamentUpdate)

  /**
   * End of the tournament update.
   */
  @messageName("updateEnd") case UpdateEnd()

  /**
   * A tournament-bound error occurred.
   */
  case Error(error: String)

  /**
   * The tournament was forcibly ended.
   */
  case ForceEnd()

  /**
   * A user joined the tournament.
   */
  case Join(user: Username)

  /**
   * A user left the tournament.
   */
  case Leave(user: Username)

  /**
   * A user was replaced by another.
   *
   * @param old the previous user
   * @param current the substitute user
   */
  case Replace(old: Username, current: Username)

  /**
   * The tournament started.
   *
   * @param numPlayers the number of participants
   */
  case Start(numPlayers: Count)

  /**
   * A user got disqualified.
   *
   * @param user the disqualified participant
   */
  case Disqualify(user: Username)

  /**
   * A tournament battle started.
   *
   * @param firstUser  the first battle participant
   * @param secondUser the second battle participant
   * @param room       the id of the battle room
   */
  case BattleStart(firstUser: Username, secondUser: Username, room: RoomId)

  /**
   * A tournament battle ended
   *
   * @param firstUser  the first battle participant
   * @param secondUser the second battle participant
   * @param result the result of the battle from `firstUser` perspective
   * @param score the current score of both players
   * @param recorded fail if the result is a draw and the tournament does not support it, success otherwise
   * @param room       the id of the battle room
   */
  case BattleEnd(
      firstUser: Username,
      secondUser: Username,
      result: BattleResult,
      score: BattleScore,
      recorded: TournamentRecord,
      room: RoomId
  )

  /**
   * The tournament ended gracefully.
   *
   * @param data details about the tournament ending
   */
  case End(data: TournamentEnd)

  /**
   * Users are now either allowed or disallowed to join other tournament battles (aka scout).
   *
   * @param setting the new rule about scouting
   */
  case Scouting(setting: TournamentSetting)

  /**
   * An update on the auto-starting policy.
   *
   * @param state the new policy on auto-starting
   */
  case AutoStart(state: TournamentAutoStart)

  /**
   * An update on the auto-disqualification policy.
   *
   * @param state the new policy on auto-disqualification
   */
  case AutoDq(state: TournamentAutoDq)

object TournamentMessage:

  given MessageDecoder[TournamentMessage] =
    for
      _ <- MessageDecoder.word("tournament")
      message <- MessageDecoder.derived[TournamentMessage]
    yield message
