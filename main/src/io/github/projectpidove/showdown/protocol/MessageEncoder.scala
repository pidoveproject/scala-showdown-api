package io.github.projectpidove.showdown.protocol

import zio.{IO, ZIO}

trait MessageEncoder[T]:

  def encode(value: T): Either[ProtocolError, List[String]]
