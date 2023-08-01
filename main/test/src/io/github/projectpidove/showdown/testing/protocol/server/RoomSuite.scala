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

    test("title") - assertDecodeString(decoder, "|title|gen9monotype3", RoomMessage.Title("gen9monotype3"))

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

    test("message") - assertDecodeString(decoder, "||Hello World!", RoomMessage.Message("Hello World!"))
    test("html") - assertDecodeString(decoder, "|html|<h1>Hello World!</h1>", RoomMessage.Html(HTML("<h1>Hello World!</h1>")))
    test("uhtml") - assertDecodeString(decoder, "|uhtml|poll|<h1>Hello World!</h1>", RoomMessage.UHtml("poll", HTML("<h1>Hello World!</h1>")))
    test("join") - assertDecodeString(decoder, "|join| Il_totore", RoomMessage.Join(Username("Il_totore", None)))
    test("leave") - assertDecodeString(decoder, "|leave| Il_totore", RoomMessage.Leave(Username("Il_totore", None)))
    test("name") - assertDecodeString(decoder, "|name| Il_totore| El_totore", RoomMessage.Name(Username("Il_totore", None), Username("El_totore", None)))
    test("chat") - assertDecodeString(decoder, "|chat| Il_totore|gl hf", RoomMessage.Chat(Username("Il_totore", None), "gl hf"))
    test("notify") - assertDecodeString(decoder, "|notify|Friend request|Il_totore invited you", RoomMessage.Notify("Friend request", "Il_totore invited you"))
    test("notify") - assertDecodeString(decoder, "|notify|Friend request|Il_totore invited you|thisisatoken", RoomMessage.NotifyHighlight("Friend request", "Il_totore invited you", "thisisatoken"))
    test("timestamp") - assertDecodeString(decoder, "|:|12345", RoomMessage.Timestamp(12345))
    test("timestampChat") - assertDecodeString(decoder, "|c:|12345| Il_totore|Hello", RoomMessage.TimestampChat(12345, Username("Il_totore", None), "Hello"))
    test("battle") - assertDecodeString(decoder, "|battle|roomid| Il_totore|*Zarel", RoomMessage.Battle(RoomId("roomid"), Username("Il_totore", None), Username("Zarel", Some('*'))))
