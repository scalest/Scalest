package scalest.admin

import scala.concurrent.Future

trait FutureToEffect[F[_]] {
  def toEffect[A](future: Future[A]): F[A]
}

object FutureToEffect {
  implicit val F2F: FutureToEffect[Future] = new FutureToEffect[Future] {
    def toEffect[A](future: Future[A]): Future[A] = future
  }

  implicit class FutureToEffectOps[T](future: Future[T]) {
    def effect[F[_]](implicit F2E: FutureToEffect[F]): F[T] = F2E.toEffect(future)
  }
}


