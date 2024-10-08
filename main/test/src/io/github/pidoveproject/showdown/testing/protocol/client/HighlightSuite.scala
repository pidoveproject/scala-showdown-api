package io.github.pidoveproject.showdown.testing.protocol.client

import io.github.pidoveproject.showdown.protocol.MessageEncoder
import io.github.pidoveproject.showdown.protocol.client.*
import io.github.pidoveproject.showdown.testing.protocol.*
import utest.*

object HighlightSuite extends TestSuite:

  val tests = Tests:

    val encoder = summon[MessageEncoder[HighlightCommand]]

    test("add") - assertEncode(encoder, HighlightCommand.Add(List("wordA", "wordB")), List("highlight", "add", "wordA", "wordB"))
    test("roomAdd") - assertEncode(encoder, HighlightCommand.RoomAdd(List("wordA", "wordB")), List("highlight", "roomadd", "wordA", "wordB"))
    test("list") - assertEncode(encoder, HighlightCommand.List, List("highlight", "list"))
    test("roomList") - assertEncode(encoder, HighlightCommand.RoomList, List("highlight", "roomlist"))
    test("delete") - assertEncode(encoder, HighlightCommand.Delete(List("wordA", "wordB")), List("highlight", "delete", "wordA", "wordB"))
    test("roomDelete") - assertEncode(encoder, HighlightCommand.RoomDelete(List("wordA", "wordB")), List("highlight", "roomdelete", "wordA", "wordB"))
    test("clear") - assertEncode(encoder, HighlightCommand.Clear, List("highlight", "clear"))
    test("roomClear") - assertEncode(encoder, HighlightCommand.RoomClear, List("highlight", "roomclear"))
    test("clearAll") - assertEncode(encoder, HighlightCommand.ClearAll, List("highlight", "clearall"))
