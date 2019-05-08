package scalest

import io.circe._
import io.circe.generic.semiauto._
import scalest.health.Statuses.Status

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
    def create[F[_]](
      isAliveP: () => F[Boolean],
      nameP: String,
      descriptionP: String,
      additionP: Json = Json.Null,
      timeoutP: FiniteDuration = 1.seconds
    ) = new HealthCheck[F] {
      def isAlive: F[Boolean] = isAliveP()
      def name: String = nameP
      def description: String = descriptionP
      override def addition: Json = addition
      override def timeout: FiniteDuration = timeoutP
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
