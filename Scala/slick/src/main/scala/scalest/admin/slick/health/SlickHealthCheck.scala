package scalest.admin.slick.health

import scalest.admin.FutureToEffect
import scalest.admin.FutureToEffect._
import scalest.health.HealthCheck
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import io.circe.Json

case class SlickHealthCheck[F[_], P <: JdbcProfile](
  dc: DatabaseConfig[P],
  override val timeout: FiniteDuration = 1.seconds,
  override val addition: Json = Json.Null
)(implicit F2E: FutureToEffect[F], ec: ExecutionContext)
    extends HealthCheck[F] {
  import dc.profile.api._

  override def isAlive: F[Boolean] = dc.db.run(sql"SELECT 1".as[Long].head.map(_ => true)).effect[F]
  override def name: String = s"Database"
  override def description: String = "Database health status"
}
