package io.github.pidoveproject.showdown.protocol

import zio.*
import zio.stream.*

enum Channel[-In, +Out]:
  case QueueBased[A](queue: Queue[A]) extends Channel[A, A]
  case Pipe[-In, Out, +Out2](in: Channel[In, Out], out: Channel[Out, Out2]) extends Channel[In, Out2]
  case Map[-In, Out, +Out2](channel: Channel[In, Out], f: Out => Out2) extends Channel[In, Out2]
  case Contramap[In, +Out, -In2](channel: Channel[In, Out], f: In2 => In) extends Channel[In2, Out]

  def take(using Trace): UIO[Out] = this match
    case QueueBased(queue)     => queue.take
    case Pipe(_, out)          => out.take
    case Map(channel, f)       => channel.take.map(f)
    case Contramap(channel, _) => channel.take

  def poll(using Trace): UIO[Option[Out]] = this match
    case QueueBased(queue)     => queue.poll
    case Pipe(_, out)          => out.poll
    case Map(channel, f)       => channel.poll.map(_.map(f))
    case Contramap(channel, _) => channel.poll

  def offer(value: In)(using Trace): UIO[Boolean] = this match
    case QueueBased(queue)     => queue.offer(value)
    case Pipe(in, _)           => in.offer(value)
    case Map(channel, _)       => channel.offer(value)
    case Contramap(channel, f) => channel.offer(f(value))

  def pipe[Out2](next: Channel[Out, Out2]): Channel[In, Out2] = Pipe(this, next)

  def >>>[Out2](next: Channel[Out, Out2]): Channel[In, Out2] = pipe(next)

  def <<<[In2](before: Channel[In2, In]): Channel[In2, Out] = before.pipe(this)

  def map[Out2](f: Out => Out2): Channel[In, Out2] = Map(this, f)

  def contramap[In2](f: In2 => In): Channel[In2, Out] = Contramap(this, f)

  def toZStream(using Trace): UStream[Out] = this match
    case QueueBased(queue)     => ZStream.fromQueue(queue)
    case Pipe(_, out)          => out.toZStream
    case Map(channel, f)       => channel.toZStream.map(f)
    case Contramap(channel, _) => channel.toZStream

object Channel:

  def fromQueue[A](queue: Queue[A]): Channel[A, A] = QueueBased(queue)

  def unbounded[A](using Trace): UIO[Channel[A, A]] = Queue.unbounded[A].map(fromQueue)
