package scalest.admin

import cats.ApplicativeError
import cats.implicits._
import scalest.error
import scalest.health.{HealthCheck, HealthCheckStatus, HealthResponse, Statuses}
import scalest.tapir._
import sttp.tapir.server.ServerEndpoint

case class HealthModule[F[_]](healthChecks: Seq[HealthCheck[F]] = Seq.empty)(
  implicit timeout: Timeout[F],
  AE: ApplicativeError[F, Throwable],
) extends TapirModule[F] {
  private def check =
    healthChecks
      .map { h =>
        timeout
          .timeoutTo(h.isAlive, h.timeout, false.pure[F])
          .handleError(_ => false)
          .map(Statuses.fromBoolean)
          .map(status => HealthCheckStatus(h.name, status, h.description, h.addition))
      }
      .toList
      .sequence
      .map { statuses =>
        val status = statuses.map(_.status).find(_ == Statuses.Down).getOrElse(Statuses.Up)
        HealthResponse(status, statuses)
      }

  val healthEndpoint: ServerEndpoint[Unit, error.CommonError, HealthResponse, Nothing, F] = {
    commonEndpoint("health").get
      .in("health")
      .out(jsonBody[HealthResponse])
      .tapir(_ => check)
  }

  override val endpoints: List[ServerEndpoint[_, _, _, Nothing, F]] = List(healthEndpoint)
}
