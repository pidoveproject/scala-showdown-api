package io.github.projectpidove.showdown.testing.protocol.server

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.projectpidove.showdown.*
import io.github.projectpidove.showdown.protocol.*
import io.github.projectpidove.showdown.protocol.server.GlobalMessage
import io.github.projectpidove.showdown.protocol.server.query.{ResponseContent, RoomInfo, Rooms, UserInfo}
import io.github.projectpidove.showdown.room.*
import io.github.projectpidove.showdown.testing.protocol.*
import io.github.projectpidove.showdown.user.*
import utest.*

object GlobalSuite extends TestSuite:

  val tests = Tests:

    val br = System.lineSeparator()
    val decoder = summon[MessageDecoder[GlobalMessage]]

    test("popup"):
      test("inline") - assertDecodeString(decoder, "|popup|hello world", GlobalMessage.Popup(PopupMessage("hello world")))
      test("newline") - assertDecodeString(decoder, "|popup|hello||world", GlobalMessage.Popup(PopupMessage(s"hello${br}world".assume)))

    test("pm"):
      test("noPipe") - assertDecodeString(decoder, "|pm| Il_totore|*Zarel|Wanna play?", GlobalMessage.PrivateMessage(
        User(Username("Il_totore"), None),
        User(Username("Zarel"), Some('*')),
        ChatMessage("Wanna play?")
      ))

      test("withPipes") - assertDecodeString(decoder, "|pm| Il_totore|*Zarel|I like pipes `|`", GlobalMessage.PrivateMessage(
        User(Username("Il_totore"), None),
        User(Username("Zarel"), Some('*')),
        ChatMessage("I like pipes `|`")
      ))

    test("usercount") - assertDecodeString(decoder, "|usercount|21838", GlobalMessage.UserCount(Count(21838)))
    test("nametaken") - assertDecodeString(decoder, "|nametaken|Il_totore|Name already taken", GlobalMessage.NameTaken("Il_totore", "Name already taken"))
    test("challstr"):
      inline val challstr = "4|38c34d953aa819f57405006c102b13e480c07ad9c87245228dc23be2f22b403050f989ca5c65aac92326d185547f4154be9e7f6c79b233a8e933fbcd98728bb349942fb0d2c4faee5fe88581d25906885a1d5006831b84a6672134d72c689883a4d071a2454a5aab2a39aaf34933060c439438e4443887ffcce6d69fbe5f9deb"
      assertDecodeString(decoder, s"|challstr|$challstr", GlobalMessage.ChallStr(ChallStr(challstr)))

    test("updateuser"):
      val settings =
        UserSettings(
          blockChallenges = false,
          blockPMs = true,
          ignoreTickets = true,
          hideBattlesFromTrainerCard = true,
          blockInvites = false,
          doNotDisturb = false,
          blockFriendRequests = false,
          allowFriendNotifications = true,
          displayBattlesToFriends = true,
          hideLogins = false,
          hiddenNextBattle = false,
          inviteOnlyNextBattle = false,
          language = Some("fr")
        )

      val settingsJson =
        """{
          |  "blockChallenges":false,
          |  "blockPMs":true,
          |  "ignoreTickets":true,
          |  "hideBattlesFromTrainerCard":true,
          |  "blockInvites":false,"doNotDisturb":false,
          |  "blockFriendRequests":false,
          |  "allowFriendNotifications":true,
          |  "displayBattlesToFriends":true,
          |  "hideLogins":false,
          |  "hiddenNextBattle":false,
          |  "inviteOnlyNextBattle":false,
          |  "language":"fr"
          |}""".stripMargin.replaceAll(raw"\w\n", "")

      assertDecodeString(
        decoder,
        s"|updateuser| Il_totore|1|kimonogirl|$settingsJson",
        GlobalMessage.UpdateUser(User(Username("Il_totore"), None), true, AvatarName("kimonogirl"), settings)
      )

    test("formats") - assertDecodeString(
      decoder,
      "|formats|,1|S/V Singles|[Gen 9] Random Battle,f|[Gen 9] Unrated Random Battle,b|,2|Other Metagames|[Gen 9] Almost Any Ability,e|[Gen 9] Balanced Hackmons,e",
      GlobalMessage.Formats(List(
        FormatCategory(
          name = FormatCategoryName("S/V Singles"),
          column = 1,
          formats = List(
            Format(FormatName("[Gen 9] Random Battle"), random = true),
            Format(FormatName("[Gen 9] Unrated Random Battle"), random = true)
          )
        ),
        FormatCategory(
          name = FormatCategoryName("Other Metagames"),
          column = 2,
          formats = List(
            Format(FormatName("[Gen 9] Almost Any Ability"), random = true),
            Format(FormatName("[Gen 9] Balanced Hackmons"), random = true),
          )
        )
      )
    ))

    test("updatesearch"):
      test("empty") - assertDecodeString(
        decoder,
        """|updatesearch|{"searching":[],"games":null}""",
        GlobalMessage.UpdateSearch(GameSearch(List.empty, Map.empty))
      )

      test("filled") - assertDecodeString(
        decoder,
        """|updatesearch|{"searching":["gen9unratedrandombattle"],"games":{"battle-gen9unratedrandombattle-1917286443":"[Gen 9] Unrated Random Battle"}}""",
        GlobalMessage.UpdateSearch(GameSearch(
          searching = List(FormatName("gen9unratedrandombattle")),
          games = Map("battle-gen9unratedrandombattle-1917286443" -> "[Gen 9] Unrated Random Battle")
        ))
      )

    test("queryresponse"):
      test("roomlist") - assertDecodeString(
        decoder,
        """|queryresponse|roomlist|{"rooms":{"battle-gen9randombattle-1918543121":{"p1":"mynameisjeeeef","p2":"xartumax","minElo":1045},"battle-gen9randombattle-1918543120":{"p1":"Szakallo90","p2":"Juancrub14","minElo":1420}}}""",
        GlobalMessage.QueryResponse(ResponseContent.RoomList(Rooms.from(
          "battle-gen9randombattle-1918543121" -> RoomInfo("mynameisjeeeef", "xartumax", 1045),
          "battle-gen9randombattle-1918543120" -> RoomInfo("Szakallo90", "Juancrub14", 1420)
        )))
      )

      test("userdetails") - assertDecodeString(
        decoder,
        """|queryresponse|userdetails|{"id":"dracoknight1011","userid":"dracoknight1011","name":"Dracoknight1011","avatar":"kimonogirl","group":" ","autoconfirmed":true,"rooms":{"thehappyplace":{},"scholastic":{},"lobby":{}}}""",
        GlobalMessage.QueryResponse(ResponseContent.UserDetails(UserInfo(
          id = "dracoknight1011",
          userId = "dracoknight1011",
          name = "Dracoknight1011",
          avatar = AvatarName("kimonogirl"),
          group = " ",
          autoConfirmed = true,
          rooms = Map(
            "thehappyplace" -> Map.empty,
            "scholastic" -> Map.empty,
            "lobby" -> Map.empty
          )
        )))
      )