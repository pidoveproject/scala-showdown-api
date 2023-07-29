package io.github.projectpidove.showdown.protocol

trait ProtocolEncoder[T]:

  def encode(value: T): List[String]