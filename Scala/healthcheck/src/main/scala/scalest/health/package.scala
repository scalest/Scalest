package scalest

import io.circe._
import io.circe.generic.semiauto._
import scalest.health.Statuses.Status

import scala.concurrent.Future
import scala.concurrent.duration._

package object health {

  trait HealthCheck[F[_]] {
    def isAlive: F[Boolean]
    def name: String
    def description: String
    def addition: Json = Json.Null
    def timeout: FiniteDuration = 1.seconds
  }

  object HealthCheck {

    /**
     * This one supposed to be used by effectful computions like IO, ZIO, Monix that are lazy
     */
    def make[F[_]](
      check: F[Boolean],
      checkName: String,
      checkDesctiption: String,
      checkAddition: Json = Json.Null,
      checkTimeout: FiniteDuration = 1.seconds,
    )(implicit notFuture: NotFuture[F]): HealthCheck[F] = new HealthCheck[F] {
      val isAlive: F[Boolean] = check
      val name: String = checkName
      val description: String = checkDesctiption
      override val addition: Json = checkAddition
      override val timeout: FiniteDuration = checkTimeout
    }

    /**
     * Specific to Future
     */
    def makeFuture(
      check: () => Future[Boolean],
      checkName: String,
      checkDesctiption: String,
      checkAddition: Json = Json.Null,
      checkTimeout: FiniteDuration = 1.seconds,
    ): HealthCheck[Future] = new HealthCheck[Future] {
      def isAlive: Future[Boolean] = check()
      val name: String = checkName
      val description: String = checkDesctiption
      override val addition: Json = checkAddition
      override val timeout: FiniteDuration = checkTimeout
    }
  }

  object Statuses extends Enumeration {
    type Status = Value
    val Up: Status = Value("UP")
    val Down: Status = Value("DOWN")

    def fromBoolean(state: Boolean): Status = if (state) Up else Down

    implicit val codec: Codec[Status] = Codec.codecForEnumeration(this)
  }

  case class HealthCheckStatus(name: String, status: Status, description: String, addition: Json)

  object HealthCheckStatus {
    implicit val codec: Codec[HealthCheckStatus] = deriveCodec
  }

  case class HealthResponse(status: Status, statuses: Seq[HealthCheckStatus])

  object HealthResponse {
    implicit val codec: Codec[HealthResponse] = deriveCodec
  }

}
