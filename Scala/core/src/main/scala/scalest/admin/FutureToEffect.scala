package scalest.admin

import cats.effect.{ContextShift, IO}

import scala.concurrent.Future

trait FutureToEffect[F[_]] {
  def toEffect[A](future: Future[A]): F[A]
}

object FutureToEffect {
  implicit val F2F: FutureToEffect[Future] = new FutureToEffect[Future] {
    def toEffect[A](future: Future[A]): Future[A] = future
  }

  implicit def F2IO(implicit CS: ContextShift[IO]): FutureToEffect[IO] = new FutureToEffect[IO] {
    override def toEffect[A](future: Future[A]): IO[A] = IO.fromFuture(IO(future))
  }

  implicit class FutureToEffectOps[T](future: Future[T]) {
    def effect[F[_]](implicit F2E: FutureToEffect[F]): F[T] = F2E.toEffect(future)
  }
}
