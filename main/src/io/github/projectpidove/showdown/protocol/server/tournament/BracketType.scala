package io.github.projectpidove.showdown.protocol.server.tournament

import zio.json.JsonDecoder

enum BracketType derives JsonDecoder:
  case Tree
