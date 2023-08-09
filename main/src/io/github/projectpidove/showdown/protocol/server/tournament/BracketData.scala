package io.github.projectpidove.showdown.protocol.server.tournament

import io.github.iltotore.iron.zioJson.given
import io.github.projectpidove.showdown.user.Username
import zio.json.*

enum BracketData(bracketType: BracketType) derives JsonDecoder:
  case Generated(bracketType: BracketType, rootNode: BracketNode) extends BracketData(bracketType)
  case NotGenerated(bracketType: BracketType, users: List[Username]) extends BracketData(bracketType)
