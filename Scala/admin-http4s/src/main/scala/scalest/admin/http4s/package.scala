package scalest.admin

import cats.Monad
import cats.effect.{ContextShift, Sync}
import org.http4s.HttpRoutes
import org.http4s.server.middleware.CORS
import scalest.tapir.TapirCommon
import sttp.tapir.server.http4s.{Http4sServerOptions, TapirHttp4sServer}

package object http4s extends TapirCommon with TapirHttp4sServer {

  implicit def customServerOptions[F[_]: Sync: ContextShift]: Http4sServerOptions[F] =
    Http4sServerOptions.default[F].copy(decodeFailureHandler = decodeFailureHandler)

  implicit def corsMiddleware[F[_]: Monad]: CorsMiddleware[F, HttpRoutes[F]] = CORS(_)
  implicit def toRoute[F[_]: Sync: ContextShift]: ToRoute[F, HttpRoutes[F]] = _.toRoutes
}
