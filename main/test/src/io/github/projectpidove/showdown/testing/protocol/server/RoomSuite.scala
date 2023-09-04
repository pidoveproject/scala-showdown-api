package io.github.projectpidove.showdown.testing.protocol.server

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.projectpidove.showdown.Timestamp
import io.github.projectpidove.showdown.protocol.*
import io.github.projectpidove.showdown.protocol.server.RoomMessage
import io.github.projectpidove.showdown.room.*
import io.github.projectpidove.showdown.user.*
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
        RoomMessage.Users(UserList.from(
          User(Username("Il_totore"), None),
          User(Username("Zarel"), Some('*')),
          User(Username("LeFanDeMeganium"), None)
        ))
      )

      test("empty") - assertDecodeString(decoder, "users|", RoomMessage.Users(UserList.empty))

    test("message") - assertDecodeString(decoder, "||Hello World!", RoomMessage.Message(ChatContent("Hello World!")))
    test("html") - assertDecodeString(decoder, "|html|<h1>Hello World!</h1>", RoomMessage.Html(HTML("<h1>Hello World!</h1>")))
    test("uhtml") - assertDecodeString(decoder, "|uhtml|poll|<h1>Hello World!</h1>", RoomMessage.UHtml("poll", HTML("<h1>Hello World!</h1>")))
    test("join"):
      test - assertDecodeString(decoder, "|join| Il_totore", RoomMessage.Join(User(Username("Il_totore"), None)))
      test - assertDecodeString(decoder, "|j| Il_totore", RoomMessage.Join(User(Username("Il_totore"), None)))
      test - assertDecodeString(decoder, "|J| Il_totore", RoomMessage.Join(User(Username("Il_totore"), None)))
    test("leave"):
      test - assertDecodeString(decoder, "|leave| Il_totore", RoomMessage.Leave(User(Username("Il_totore"), None)))
      test - assertDecodeString(decoder, "|l| Il_totore", RoomMessage.Leave(User(Username("Il_totore"), None)))
      test - assertDecodeString(decoder, "|L| Il_totore", RoomMessage.Leave(User(Username("Il_totore"), None)))
    test("name") - assertDecodeString(decoder, "|name| Il_totore| El_totore", RoomMessage.Name(User(Username("Il_totore"), None), User(Username("El_totore"), None)))
    test("chat"):
      test("noPipe") - assertDecodeString(decoder, "|chat| Il_totore|gl hf", RoomMessage.Chat(User(Username("Il_totore"), None), ChatContent("gl hf")))
      test("withPipes") - assertDecodeString(decoder, "|chat| Il_totore|gl hf|hello|world", RoomMessage.Chat(User(Username("Il_totore"), None), ChatContent("gl hf|hello|world")))
    test("notify") - assertDecodeString(decoder, "|notify|Friend request|Il_totore invited you", RoomMessage.Notify("Friend request", "Il_totore invited you", None))
    test("notify") - assertDecodeString(decoder, "|notify|Friend request|Il_totore invited you|thisisatoken", RoomMessage.Notify("Friend request", "Il_totore invited you", Some(HighlightToken("thisisatoken"))))
    test("timestamp") - assertDecodeString(decoder, "|:|12345", RoomMessage.Timestamp(Timestamp(12345)))
    test("timestampChat") - assertDecodeString(decoder, "|c:|12345| Il_totore|Hello", RoomMessage.TimestampChat(Timestamp(12345), User(Username("Il_totore"), None), ChatContent("Hello")))
    test("battle") - assertDecodeString(decoder, "|battle|roomid| Il_totore|*Zarel", RoomMessage.Battle(RoomId("roomid"), User(Username("Il_totore"), None), User(Username("Zarel"), Some('*'))))
