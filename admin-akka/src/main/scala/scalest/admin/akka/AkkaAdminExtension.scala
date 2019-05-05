package scalest.admin.akka

import akka.http.scaladsl.server.Route
import scalest.admin.ModelInfo
class AkkaAdminExtension(modelAdmins: Seq[AkkaModelAdmin[_, _]]) extends AuthDirectives with CorsDirectives with HttpService {

  val schemas: Seq[ModelInfo] = modelAdmins.map(_.info)

  val route: Route = cors {
    staticRoute ~ adminRoute ~ apiRoute
  }

  def adminRoute: Route = pathPrefix("admin") {
    homeRoute ~ loginRoute ~ infoRoute
  }

  private def homeRoute = {
    pathEndOrSingleSlash {
      getFromResource("vue/index.html")
    }
  }

  private def infoRoute = {
    auth {
      pathPrefix("info") {
        pathEndOrSingleSlash {
          complete(schemas)
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

object AkkaAdminExtension {
  def apply(mas: AkkaModelAdmin[_, _]*): AkkaAdminExtension = new AkkaAdminExtension(mas)
}
