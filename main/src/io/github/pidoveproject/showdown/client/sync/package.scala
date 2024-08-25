package io.github.pidoveproject.showdown.client.sync

import zio.*

extension [R, E <: Throwable, A](effect: ZIO[R, E, A])
  def runThrowFailure(runtime: Runtime[R]): A = Unsafe.unsafe { implicit unsafe =>
    runtime.unsafe.run(effect).getOrThrowFiberFailure()
  }
