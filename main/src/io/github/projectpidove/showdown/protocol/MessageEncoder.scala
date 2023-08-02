package io.github.projectpidove.showdown.protocol

trait MessageEncoder[T]:

  def encode(value: T): List[String]