package io.github.pidoveproject.showdown.protocol.server

import io.github.pidoveproject.showdown.protocol.MessageDecoder

type BattleMessage = BattleAttackMessage
  | BattleInitializationMessage
  | BattleMajorActionMessage
  | BattleMinorActionMessage
  | BattleProgressMessage
  | BattleStatusMessage

type ServerMessage = GlobalMessage | RoomBoundMessage | TournamentMessage

object ServerMessage:

  def decoder: MessageDecoder[ServerMessage] = MessageDecoder.derivedUnion[ServerMessage]