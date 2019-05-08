package scalest.admin.akka

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{Directives, Route}
import cats.implicits._
import scalest.admin.akka.AkkaAdminExtension.akkaTimeout
import scalest.admin.akka.tapir._
import scalest.admin.{AdminModule, AuthModule, HealthModule, ModelAdmin, SwaggerModule, TapirModule, Timeout}
import scalest.auth.AuthService
import scalest.health._
import sttp.tapir.openapi.Info
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Future, Promise}

case class AkkaAdminExtension(
  modelAdmins: Seq[ModelAdmin[Future, _, _, _]] = Seq.empty,
  healthChecks: Seq[HealthCheck[Future]] = Seq.empty,
  customAuthService: Option[AuthService[Future]] = None
)(implicit system: ActorSystem)
    extends Directives
    with CorsDirectives {
  import system._

  val authService: AuthService[Future] = customAuthService.getOrElse(AuthService.default[Future])
  val modules: List[TapirModule[Future]] = List(
    HealthModule(healthChecks),
    AuthModule(authService),
    AdminModule(modelAdmins)
  ) ++ modelAdmins
  val endpoints: List[ServerEndpoint[_, _, _, Nothing, Future]] = modules.flatMap(_.routes)
  val swagger: SwaggerModule[Future] = SwaggerModule(Info("API", "0.0.0"), endpoints)
  val routes: Route = cors(indexRoute ~ staticRoute ~ endpoints.toRoute ~ swagger.routes.toRoute)

  //STATIC SERVE
  def indexRoute: Route = staticResource("admin", "admin-ui/index.html") ~ staticResource("scalest_logo", "scalest_logo.png")
  def staticRoute: Route = adminStaticRoute ~ miscStaticRoute
  def miscStaticRoute: Route = staticResources("favicon.ico", "scalest.png", "material.css")
  def adminStaticRoute: Route = pathPrefix("static")(getFromResourceDirectory("admin-ui/static"))
  def staticResources(names: String*): Route =
    names.map(n => staticResource(n, s"admin-ui/$n")).foldLeft[Route](reject)(_ ~ _)
  def staticResource(prefix: String, resource: String): Route =
    pathPrefix(prefix)(pathEndOrSingleSlash(getFromResource(resource)))

  //BUILDER METHODS
  def withModelAdmins(modelAdmins: ModelAdmin[Future, _, _, _]*): AkkaAdminExtension = copy(modelAdmins = modelAdmins)
  def withHealthChecks(healthChecks: HealthCheck[Future]*): AkkaAdminExtension = copy(healthChecks = healthChecks)
  def withAuthService(authService: AuthService[Future]): AkkaAdminExtension = copy(customAuthService = authService.some)
}

object AkkaAdminExtension {
  implicit def akkaTimeout(implicit system: ActorSystem): Timeout[Future] = new Timeout[Future] {
    import system._

    override def timeoutTo[A](fa: Future[A], duration: FiniteDuration, fallback: Future[A]): Future[A] = {
      val promise = Promise[A]
      scheduler.scheduleOnce(duration)(promise.completeWith(fallback))
      Future.firstCompletedOf(Seq(fa, promise.future))
    }
  }
}
