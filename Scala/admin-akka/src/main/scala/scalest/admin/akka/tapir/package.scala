package scalest.admin.akka

import cats.implicits._
import scalest.exception.ModelValidationException
import scalest.tapir.TapirCommon
import scalest.validation._
import sttp.tapir.server.akkahttp._

import scala.concurrent.Future

package object tapir extends TapirCommon with TapirAkkaHttpServer {

  implicit val customServerOptions: AkkaHttpServerOptions = AkkaHttpServerOptions.default.copy(decodeFailureHandler = decodeFailureHandler)

  def validate[E](entity: E)(implicit validator: Validator[E]): Future[E] = {
    validator.validate(entity)
      .leftMap(collectErrors)
      .toEither
      .leftMap(ModelValidationException)
      .fold(Future.failed, Future.successful)
  }
}