package io.github.projectpidove.showdown.protocol.client

import io.github.projectpidove.showdown.protocol.client.GroupTarget
import io.github.projectpidove.showdown.protocol.MessageEncoder
import io.github.projectpidove.showdown.FormatName
import io.github.projectpidove.showdown.room.{ChatMessage, RoomId, given}
import io.github.projectpidove.showdown.user.{Username, given}
enum DataCommand derives MessageEncoder:
  case Data(data: Option[List[Option[String]]])
  case DexSearch(data: Option[List[Option[String]]])
  case MoveSearch(data: Option[List[Option[String]]])
  case ItemSearch(data: Option[List[Option[String]]])
  case Learn() // TODO later
  case StatCalc() // TODO later
  case Effectiveness(data: Option[List[Option[String]]])
  case Weakness(data: Option[List[Option[String]]])
  case Coverage(data: Option[List[Option[String]]])
  case RandomMove(data: Option[List[Option[String]]])
  case RandomPokemon(data: Option[List[Option[String]]])
