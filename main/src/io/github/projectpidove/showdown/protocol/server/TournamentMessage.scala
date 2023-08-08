package io.github.projectpidove.showdown.protocol.server

import io.github.projectpidove.showdown.{Count, FormatName}
import io.github.projectpidove.showdown.protocol.{MessageDecoder, MessageName}
import io.github.projectpidove.showdown.protocol.server.tournament.{BattleScore, TournamentAutoDq, TournamentAutoStart, TournamentGenerator, TournamentRecord, BattleResult, TournamentSetting}
import io.github.projectpidove.showdown.protocol.MessageDecoder.given
import io.github.projectpidove.showdown.room.RoomId
import io.github.projectpidove.showdown.user.{User, Username}

enum TournamentMessage:
  case Create(format: FormatName, generator: TournamentGenerator, playerCap: Count)
  // TODO Update
  @MessageName("updateEnd") case UpdateEnd()
  case Error(error: String)
  case ForceEnd()
  case Join(user: Username)
  case Leave(user: Username)
  case Replace(old: Username, current: Username)
  case Start(numPlayers: Count)
  case Disqualify(user: Username)
  case BattleStart(user1: Username, user2: Username, roomID: RoomId)
  case BattleEnd(
                  user1: Username,
                  user2: Username,
                  result: BattleResult,
                  score: BattleScore,
                  recorded: TournamentRecord,
                  roomID: RoomId
  )
  // TODO end() cause JSON
  case Scouting(setting: TournamentSetting)
  case AutoStart(state: TournamentAutoStart)
  case AutoDq(state: TournamentAutoDq)

object TournamentMessage:

  given MessageDecoder[TournamentMessage] =
    for
      _ <- MessageDecoder.word("tournament")
      message <- MessageDecoder.derived[TournamentMessage]
    yield message
