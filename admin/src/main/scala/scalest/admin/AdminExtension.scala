package scalest.admin

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Route
import scalest.ScalestService

class AdminExtension(modelAdmins: List[ModelAdmin[_]])
  extends ScalestService {

  val route: Route = pathPrefix("admin") {
    modelAdmins.map { ma =>
      pathPrefix(ma.modelName) {
        complete(
          HttpEntity(
            ContentTypes.`text/html(UTF-8)`,
            ma.html
          )
        )
      }
    }.reduce(_ ~ _)
  } ~ pathPrefix("api") {
    modelAdmins.map(_.apiRoute).reduce(_ ~ _)
  }
}
