package scalest.health

import scala.annotation.implicitAmbiguous
import scala.concurrent.Future

sealed trait NotFuture[-F[_]]

object NotFuture extends NotFuture[Any] {
  implicit final def notFuture[F[_]]: NotFuture[F] = NotFuture
  @implicitAmbiguous("This operation only makes sense for lazy effects")
  implicit final val notFutureAmbiguous1: NotFuture[Future] = NotFuture
  implicit final val notFutureAmbiguous2: NotFuture[Future] = NotFuture
}
