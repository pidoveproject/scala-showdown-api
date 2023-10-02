package io.github.projectpidove.showdown.protocol.server

type BattleMessage = BattleAttackMessage
  | BattleInitializationMessage
  | BattleMajorActionMessage
  | BattleMinorActionMessage
  | BattleProgressMessage
  | BattleStatusMessage

type ServerMessage = GlobalMessage | RoomBoundMessage | TournamentMessage
