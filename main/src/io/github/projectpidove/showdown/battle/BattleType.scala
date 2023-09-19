package io.github.projectpidove.showdown.battle

import io.github.projectpidove.showdown.protocol.MessageDecoder

enum BattleType derives MessageDecoder:
  case Singles()
  case Doubles()
  case Triples()
  case Multi()
  case FreeForAll()