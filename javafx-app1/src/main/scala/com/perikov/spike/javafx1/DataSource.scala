package com.perikov.spike.javafx1

import java.time.Instant
import scala.concurrent.duration.*
import cats.effect.std.Random

case class Measurement(instant: Instant, value: Double)

type DataSource[F[_]] = fs2.Stream[F, Measurement]

trait Generator[F[_]]:
  def setPeriod(period: FiniteDuration): F[Unit]
  def period: F[FiniteDuration]
  def setNoiseLevel(noiseLevel: Double): F[Unit]
  val data: DataSource[F]

object Generator:
  import cats.*
  import cats.implicits.*
  import cats.effect.*
  import cats.effect.implicits.*
  import fs2.Stream

  def apply[F[_]: Ref.Make: Temporal: Random](period: FiniteDuration, sampleRate:FiniteDuration): F[Generator[F]] =
    val T = Temporal[F]
    val R = Random[F]
    (
      T.realTime.flatMap(Ref.of),
      Ref.of(period)
    ).mapN { (lastRef, periodRef) =>
      new :
        def setPeriod(period: FiniteDuration): F[Unit] =
          periodRef.set(period)
        def period: F[FiniteDuration]                  = periodRef.get
        def setNoiseLevel(noiseLevel: Double): F[Unit] = T.unit
        val data: DataSource[F]                        =
          def str(next: FiniteDuration): Stream[F, FiniteDuration] = 
            Stream.eval((lastRef.get,periodRef.get, T.realTime).tupled).flatMap {(last,period, now) =>
              val next = last + period
              Stream.emit(now) ++ Stream.sleep_(next - now) ++ str(next)
            }
          for 
            now <- Stream.eval(T.realTime)

          yield ???
        end data
             
      end new
    }
