package io.github.pidoveproject.showdown.example

import io.github.iltotore.iron.*
import io.github.pidoveproject.showdown.battle.*
import io.github.pidoveproject.showdown.protocol.client.BattleChoice
import io.github.pidoveproject.showdown.protocol.server.choice.{ActiveChoice, ChoiceRequest, MoveChoice, PokemonChoice}
import io.github.pidoveproject.showdown.room.{ChatMessage, HTML, JoinedRoom, RoomChat, RoomId, RoomType}
import io.github.pidoveproject.showdown.user.User
import tyrian.Html
import tyrian.Html.*

def viewMessage(message: ChatMessage): Option[Html[ClientMessage]] = message match
  case ChatMessage.Server(content) => Some(span(s"[SERVER] $content"))
  case ChatMessage.Sent(_, content) if content.value.startsWith("/uhtml") => None
  case ChatMessage.Sent(sender, s"/raw $content") => Some(raw("raw")()(content))
  case ChatMessage.Sent(sender, content) => Some(span(s"[${sender.name}] $content"))
  case ChatMessage.Html(content) => Some(raw("html")()(content.value))
  case ChatMessage.UHtml(name, content) => Some(raw(s"html")()(content.value))
  case ChatMessage.Join(user) => Some(span(s"[+] ${user.name}"))
  case ChatMessage.Leave(user) => Some(span(s"[-] ${user.name}"))
  case ChatMessage.Challenge(opponent, format) => Some(span("Not implemented"))

def viewChatRoom(room: JoinedRoom): Html[ClientMessage] =
  val messages =
    room
      .chat
      .messages
      .flatMap(viewMessage)

  div(
    h3(s"${room.users.size} connected"),
    div(`class` := "chat")(messages)
  )

def viewTeamMember(isActive: Boolean)(pokemon: TeamMember): Html[ClientMessage] =
  val itemView = pokemon.item match
    case HeldItem.Revealed(item, _) => label(s"Item: $item")
    case HeldItem.Destroyed(item, _) => label(s"Item: $item")
    case HeldItem.Unknown => label("Unknown item")

  val conditionView = pokemon.condition match
    case Condition(Health(current, max), Some(status)) =>
      div(
        label(s"$current/$max"),
        label(status.value)
      )
    case Condition(Health(current, max), None) =>
      label(s"$current/$max")

  val status =
    if isActive then "active"
    else pokemon.condition.status.fold("inactive")(_.value)

  div(`class` := s"$status member")(
    label(`class` := "pokemonName")(pokemon.details.species.value),
    itemView,
    conditionView
  )

val viewEmptySlot: Html[ClientMessage] =
  div(
    label("Unknown pokemon")
  )

def viewTeam(activeSlots: Set[TeamSlot])(team: PlayerTeam): Html[ClientMessage] =
  val slots =
    for slot <- 1 to team.size.value yield
      team.getPokemon(TeamSlot.assume(slot)).fold(viewEmptySlot)(viewTeamMember(activeSlots.contains(TeamSlot.assume(slot))))

  div(`class` := "teamMembers")(slots.toList)

def viewPlayer(player: Player, activeSlots: Set[TeamSlot]): Html[ClientMessage] =
  div(`class` := "playerTeam")(
    label(`class` := "playerName")(player.name.fold("???")(_.value)),
    player.team.fold(div())(viewTeam(activeSlots))
  )

def viewMoveChoice(room: RoomId, requestId: Option[Int])(choice: MoveChoice): Html[ClientMessage] =
  button(
    `class` := "moveChoice",
    onClick(ClientMessage.ChooseAction(room, BattleChoice.Move(choice.name, None, None), requestId))
  )(s"${choice.name} (${choice.pp}/${choice.maxPP})")

def viewTeamChoice(room: RoomId, requestId: Option[Int])(choice: PokemonChoice, slot: Int): Html[ClientMessage] =
  val condition = choice.condition match
    case Condition(Health(min, max), None) => s"$min/$max"
    case Condition(Health(min, max), Some(effect)) => s"$min/$max $effect"

  button(
    `class` := "teamChoice",
    onClick(ClientMessage.ChooseAction(room, BattleChoice.Switch(TeamSlot.assume(slot+1)), requestId))
  )(s"${choice.details.species} ($condition)")

def viewActiveChoice(room: RoomId, requestId: Option[Int])(choice: ActiveChoice): Html[ClientMessage] =
  div(choice.moves.map(viewMoveChoice(room, requestId)))

def viewChoice(room: RoomId)(choice: ChoiceRequest): Html[ClientMessage] = div(
  div(id := "teamChoices")(choice.team.pokemon.zipWithIndex.map(viewTeamChoice(room, choice.requestId))),
  div(id := "moveChoices")(choice.active.map(viewActiveChoice(room, choice.requestId))),
  button(onClick(ClientMessage.ChooseAction(room, BattleChoice.Undo, choice.requestId)))("Cancel")
)

def viewBattle(room: RoomId)(battle: Battle): Html[ClientMessage] =
  val playersWithActive =
    battle.players.map:(n, p) =>
      (
        p,
        battle.activePokemon.collect {
          case (pos, pokemon) if pos.player == n => pokemon.teamSlot
        }.toSet
      )

  val teams = div(id := "teams")(
    playersWithActive.map(viewPlayer).toList
  )

  val choice = battle.currentRequest.fold(div())(viewChoice(room))

  div(
    teams,
    choice,
    button(onClick(ClientMessage.Forfeit(room)))("Forfeit")
  )

def viewBattleRoom(room: JoinedRoom): Html[ClientMessage] =
  room.battle.fold(div())(viewBattle(room.id))

def viewRoom(room: JoinedRoom): Html[ClientMessage] =
  room.roomType match
    case Some(RoomType.Chat) => viewChatRoom(room)
    case Some(RoomType.Battle) => viewBattleRoom(room)
    case None => div()

def viewPrivateMessages(chat: RoomChat): Html[ClientMessage] =
  val messages =
    chat
      .messages
      .flatMap(viewMessage)

  div(`class` := "chat")(messages)

