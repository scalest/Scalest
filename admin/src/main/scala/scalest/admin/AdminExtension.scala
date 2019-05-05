package scalest.admin

import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

class AdminExtension(modelAdmins: Seq[ModelAdmin[_, _]]) extends AuthDirectives with CorsDirectives with ErrorAccumulatingCirceSupport {

  val modelAdminSchemas: Seq[ModelSchema[_]] = modelAdmins.map(_.modelSchema)

  val route: Route = corsSupport {
    staticRoute ~ adminRoute ~ apiRoute
  }

  def adminRoute: Route = pathPrefix("admin") {
    homeRoute ~ loginRoute ~ schemasRoute
  }

  private def homeRoute = {
    pathEndOrSingleSlash {
      getFromResource("vue/index.html")
    }
  }

  private def schemasRoute = {
    auth {
      pathPrefix("schemas") {
        pathEndOrSingleSlash {
          complete(modelAdminSchemas)
        }
      }
    }
  }

  def apiRoute: Route = {
    auth {
      pathPrefix("api") {
        modelAdmins.map(_.route).reduce(_ ~ _)
      }
    }
  }

  def staticRoute: Route = {
    pathPrefix("static")(getFromResourceDirectory("vue/static")) ~
      pathPrefix("favicon.ico")(getFromResource("vue/favicon.ico")) ~
      pathPrefix("scalest.png")(getFromResource("vue/scalest.png"))
  }
}

object AdminExtension {
  def apply(modelAdmins: ModelAdmin[_, _]*): AdminExtension = new AdminExtension(modelAdmins)

  case class AppError(error: String)

  object AppError {
    implicit val encoder: Encoder[AppError] = deriveEncoder
  }

}
