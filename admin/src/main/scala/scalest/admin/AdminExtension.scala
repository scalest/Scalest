package scalest.admin

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.{Directives, Route}
import scalest.admin.ModelAdminTemplate._

class AdminExtension(modelAdmins: ModelAdmin[_]*)
  extends Directives {

  val header: String = generateHeader(modelAdmins)

  val route: Route = pathPrefix("admin") {
    modelAdmins.map { ma =>
      val html = generateSingleModelHtml(header, ma)

      pathPrefix(ma.modelView.modelName) {
        complete(
          HttpEntity(
            ContentTypes.`text/html(UTF-8)`,
            html
          )
        )
      }
    }.reduce(_ ~ _)
  } ~ pathPrefix("api") {
    modelAdmins.map(_.route).reduce(_ ~ _)
  } ~ pathPrefix("static")(getFromResourceDirectory("static"))
  //Todo: Create auth for admin panel
}
