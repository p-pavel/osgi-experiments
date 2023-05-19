package com.perikov.osgi4cats.framework

import cats.effect.{Async, Resource}
import cats.implicits.*
import cats.effect.implicits.*
import cats.implicits.*
import cats.effect.kernel.Clock
import scala.concurrent.duration.*

class SpikeActivator extends BundleActivator:
  override protected def start[F[_]](
      ctx: BundleContext[F]
  )(using F: Async[F]): Resource[F, Unit] =
    def printState =
      (ctx.getBundle.getState
        .flatMap(st => F.delay(println(s"Bundle state is $st")))
        .andWait(1000.millis)
        .foreverM)
    for
      _ <- Resource
             .make(F.delay(println("Hello from SpikeActivator")))(_ =>
               F.delay(println("Goodbye from SpikeActivator"))
             )
      _ <- printState.background
    yield ()
