package io.github.projectpidove.showdown.testing.protocol.server

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.projectpidove.showdown.protocol.*
import io.github.projectpidove.showdown.protocol.server.RoomMessage
import io.github.projectpidove.showdown.room.*
import io.github.projectpidove.showdown.user.Username
import io.github.projectpidove.showdown.testing.protocol.*
import utest.*

object RoomSuite extends TestSuite:

  val tests = Tests:

    val decoder = summon[MessageDecoder[RoomMessage]]

    test("init"):
      test("battle") - assertDecodeString(decoder, "|init|battle", RoomMessage.Init(RoomType.Battle))
      test("chat") - assertDecodeString(decoder, "|init|chat", RoomMessage.Init(RoomType.Chat))
      test("invalid") - assertFailString(decoder, "|init|???")

    test("title") - assertDecodeString(decoder, "|title|gen9monotype3", RoomMessage.Title(RoomTitle("gen9monotype3")))

    test("users"):
      test("many") - assertDecodeString(
        decoder,
        "|users| Il_totore,*Zarel, LeFanDeMeganium",
        RoomMessage.Users(UserList(
          Username("Il_totore", None),
          Username("Zarel", Some('*')),
          Username("LeFanDeMeganium", None)
        ))
      )

      test("empty") - assertDecodeString(decoder, "users|", RoomMessage.Users(UserList()))