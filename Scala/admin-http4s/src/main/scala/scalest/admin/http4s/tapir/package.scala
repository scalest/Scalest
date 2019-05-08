package scalest.admin.http4s


import cats.effect.{ContextShift, Sync}
import cats.implicits._
import org.http4s.{DecodeResult => _}
import scalest.exception.ModelValidationException
import scalest.tapir.TapirCommon
import scalest.validation._
import sttp.tapir.server.http4s._

package object tapir extends TapirCommon with TapirHttp4sServer {

  implicit def customServerOptions[F[_] : Sync : ContextShift]: Http4sServerOptions[F] = Http4sServerOptions.default[F].copy(decodeFailureHandler = decodeFailureHandler)

  def validate[F[_], E](entity: E)(implicit validator: Validator[E], S: Sync[F]): F[E] = {
    validator.validate(entity)
      .leftMap(collectErrors)
      .toEither
      .leftMap(ModelValidationException)
      .fold(_.raiseError[F, E], _.pure[F])
  }
}
