package io.github.projectpidove.showdown.protocol.server.tournament

import zio.json.JsonDecoder

enum BattleState derives JsonDecoder:
  case Challenging
  case InProgress
  case Finished
  case Unavailable
