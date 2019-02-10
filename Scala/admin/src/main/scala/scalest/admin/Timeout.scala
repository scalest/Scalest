package scalest.admin

import cats.effect.{Concurrent, ConcurrentEffect, Timer}

import scala.concurrent.duration.FiniteDuration

trait Timeout[F[_]] {
  def timeoutTo[A](fa: F[A], duration: FiniteDuration, fallback: F[A]): F[A]
}

object Timeout {
  implicit def effectTimeout[F[_]: Timer: ConcurrentEffect]: Timeout[F] = new Timeout[F] {
    override def timeoutTo[A](fa: F[A], duration: FiniteDuration, fallback: F[A]): F[A] =
      Concurrent.timeoutTo(fa, duration, fallback)
  }
}
