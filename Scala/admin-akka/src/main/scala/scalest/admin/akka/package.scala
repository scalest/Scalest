package scalest.admin

import _root_.akka.actor.ActorSystem
import _root_.akka.http.scaladsl.server.Route
import scalest.admin.akka.cors.corsWrap
import scalest.tapir.TapirCommon
import sttp.tapir.server.akkahttp.{AkkaHttpServerOptions, TapirAkkaHttpServer}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Future, Promise}

package object akka extends TapirCommon with TapirAkkaHttpServer {
  implicit val corsMiddleware: CorsMiddleware[Future, Route] = corsWrap(_)
  implicit val toRoute: ToRoute[Future, Route] = _.toRoute
  implicit def akkaTimeout(implicit system: ActorSystem): Timeout[Future] = new Timeout[Future] {
    import system._
    override def timeoutTo[A](fa: Future[A], duration: FiniteDuration, fallback: Future[A]): Future[A] = {
      val promise = Promise[A]
      scheduler.scheduleOnce(duration)(promise.completeWith(fallback))
      Future.firstCompletedOf(Seq(fa, promise.future))
    }
  }

  implicit val customServerOptions: AkkaHttpServerOptions =
    AkkaHttpServerOptions.default.copy(decodeFailureHandler = decodeFailureHandler)
}
