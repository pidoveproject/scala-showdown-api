package io.github.projectpidove.showdown.protocol.server.query

import io.github.projectpidove.showdown.protocol.{MessageDecoder, given}
import zio.json.*

case class RoomInfo(p1: String, p2: String, minElo: Int) derives JsonDecoder
