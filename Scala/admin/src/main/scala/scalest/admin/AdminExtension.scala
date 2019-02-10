package scalest.admin

import cats.ApplicativeError
import com.typesafe.scalalogging.Logger
import scalest.health.HealthCheck
import sttp.tapir.server.ServerEndpoint

trait ToRoute[F[_], R] {
  def apply(endpoints: List[ServerEndpoint[_, _, _, Nothing, F]]): R
}

trait CorsMiddleware[F[_], R] {
  def apply(route: R): R
}

case class AdminExtension[F[_]](
  modules: List[TapirModule[F]] = List.empty,
)(implicit AE: ApplicativeError[F, Throwable]) { extension =>
  val logger: Logger = Logger("scalest.AdminExtension")
  val endpoints: List[ServerEndpoint[_, _, _, Nothing, F]] = modules.flatMap(_.endpoints)
  def routes[R](
    implicit
    toRoute: ToRoute[F, R],
    corsMiddleware: CorsMiddleware[F, R],
  ): R = corsMiddleware(toRoute(endpoints))

  //BUILDER METHODS
  def withModelAdmins(modelAdmins: ModelAdmin[F, _, _]*): AdminExtension[F] =
    withModules(modelAdmins :+ ProtocolModule(modelAdmins))
  def withHealthChecks(healthChecks: HealthCheck[F]*)(implicit T: Timeout[F]): AdminExtension[F] =
    withModule(HealthModule[F](healthChecks))
  def withModule(module: TapirModule[F]): AdminExtension[F] = withModules(Seq(module))
  def withModules(modules: Seq[TapirModule[F]]): AdminExtension[F] = copy(modules = extension.modules ++ modules)
}
