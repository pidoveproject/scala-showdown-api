package io.github.pidoveproject.showdown.client

import cats.effect.IO
import tyrian.Cmd

extension [E, A](cmd: Cmd[IO, Either[E, A]])

  def orPrintError: Cmd[IO, A] = cmd.flatMap:
    case Right(value) => value
    case Left(error)  => println(error)