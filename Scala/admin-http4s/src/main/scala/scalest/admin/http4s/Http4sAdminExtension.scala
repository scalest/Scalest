package scalest.admin.http4s

import java.util.concurrent.Executors

import cats.effect.{Blocker, ConcurrentEffect, ContextShift, Timer}
import cats.implicits._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.middleware.CORS
import org.http4s.server.staticcontent._
import org.http4s.{StaticFile, _}
import scalest.admin.http4s.tapir._
import scalest.admin.{AdminModule, AuthModule, HealthModule, ModelAdmin, SwaggerModule, TapirModule}
import scalest.auth.AuthService
import scalest.health._
import sttp.tapir.openapi.Info
import sttp.tapir.server.ServerEndpoint

case class Http4sAdminExtension[F[_]](
  modelAdmins: Seq[ModelAdmin[F, _, _, _]] = Seq.empty,
  healthChecks: Seq[HealthCheck[F]] = Seq.empty,
  customAuthService: Option[AuthService[F]] = None
)(implicit CS: ContextShift[F], T: Timer[F], C: ConcurrentEffect[F])
    extends Http4sDsl[F] {
  val authService: AuthService[F] = customAuthService.getOrElse(AuthService.default[F])
  val blocker: Blocker = Blocker.liftExecutorService(Executors.newFixedThreadPool(4))
  val modules: List[TapirModule[F]] = List(
    HealthModule(healthChecks),
    AuthModule(authService),
    AdminModule(modelAdmins)
  ) ++ modelAdmins
  val endpoints: List[ServerEndpoint[_, _, _, Nothing, F]] = modules.flatMap(_.routes)
  val swagger: SwaggerModule[F] = SwaggerModule(Info("API", "0.0.0"), endpoints)
  val routes: HttpRoutes[F] = CORS(staticRoutes <+> endpoints.toRoutes <+> swagger.routes.toRoutes)

  def serveStatic(resource: String): F[Response[F]] =
    StaticFile.fromResource[F](resource, blocker).getOrElseF(NotFound())
  def staticRoutes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / resource
          if Set("favicon.ico", "scalest.png", "material.css, scalest_logo.png").contains(resource) =>
        serveStatic(s"/admin-ui/$resource")
      case GET -> Root / "admin" => serveStatic("/admin-ui/index.html")
      case GET -> Root / "scalest_logo" => serveStatic("/scalest_logo.png")
    } <+> resourceService[F](ResourceService.Config[F]("/admin-ui/static", blocker, "/static"))

  //BUILDER METHODS
  def withModelAdmins(modelAdmins: ModelAdmin[F, _, _, _]*): Http4sAdminExtension[F] = copy(modelAdmins = modelAdmins)
  def withHealthChecks(healthChecks: HealthCheck[F]*): Http4sAdminExtension[F] = copy(healthChecks = healthChecks)
  def withAuthService(authService: AuthService[F]): Http4sAdminExtension[F] = copy(customAuthService = authService.some)
}
