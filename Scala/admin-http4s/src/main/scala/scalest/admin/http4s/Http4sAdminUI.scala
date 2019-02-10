package scalest.admin.http4s

import java.util.concurrent.{ExecutorService, Executors}

import cats.effect.{Blocker, ContextShift, Effect, Sync}
import cats.implicits._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.staticcontent.{resourceService, ResourceService}

object Http4sAdminUI {
  private val availableProcessors: Int = Runtime.getRuntime.availableProcessors
  private val blockerES: ExecutorService = Executors.newFixedThreadPool(availableProcessors)
  private val DefaultBlocker: Blocker = Blocker.liftExecutorService(blockerES)

  def apply[F[_]: Sync: ContextShift: Effect](blocker: Blocker = DefaultBlocker): HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._
    def serveStatic(resource: String): F[Response[F]] =
      StaticFile.fromResource[F](resource, blocker).getOrElseF(NotFound())

    HttpRoutes.of[F] {
      case GET -> Root / resource
          if Set("favicon.ico", "scalest.png", "material.css, scalest_logo.png").contains(resource) =>
        serveStatic(s"/admin-ui/$resource")
      case GET -> Root / "admin"        => serveStatic("/admin-ui/index.html")
      case GET -> Root / "scalest_logo" => serveStatic("/scalest_logo.png")
    } <+> resourceService[F](ResourceService.Config[F]("/admin-ui/static", blocker, "/static"))
  }
}
