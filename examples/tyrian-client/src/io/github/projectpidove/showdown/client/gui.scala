package io.github.projectpidove.showdown.client

import io.github.iltotore.iron.*
import io.github.projectpidove.showdown.room.{ChatMessage, HTML, JoinedRoom}
import io.github.projectpidove.showdown.user.User
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

def viewRoom(room: JoinedRoom): Html[ClientMessage] =
  val messages =
    room
      .chat
      .messages
      .flatMap(viewMessage)
      .flatMap(List(_, br()))

  div(
    h3(s"${room.users.size} connected"),
    div(messages)
  )