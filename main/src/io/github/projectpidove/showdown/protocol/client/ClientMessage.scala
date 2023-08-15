package io.github.projectpidove.showdown.protocol.client

import io.github.projectpidove.showdown.protocol.{MessageEncoder, ProtocolError}

trait ClientMessage

object ClientMessage:
  
  given MessageEncoder[ClientMessage] with

    override def encode(value: ClientMessage): Either[ProtocolError, List[String]] = Right(Nil)