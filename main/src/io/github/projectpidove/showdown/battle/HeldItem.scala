package io.github.projectpidove.showdown.battle

import io.github.projectpidove.showdown.team.ItemName

enum HeldItem:
  case Revealed(item: ItemName, cause: Option[Effect])
  case Destroyed(item: ItemName, cause: Option[Effect])
  case Unknown