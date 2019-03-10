package scalest.admin

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpHeader}
import akka.http.scaladsl.server.{Directives, Route, StandardRoute}
import scalest.admin.ModelAdminTemplate._

import scala.collection.immutable

class AdminExtension(modelAdmins: ModelAdmin[_, _]*)
  extends Directives {

  val noCorsHeaders: immutable.Seq[HttpHeader] = immutable.Seq(
    RawHeader("Access-Control-Allow-Origin", "*"),
    RawHeader("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE"),
    RawHeader("Access-Control-Allow-Headers", "Content-Type")
  )

  val header: String = generateHeader(modelAdmins)

  val mainPageHtml: String = generateMainPageHtml(header)

  val modelAdminRoutes: Route = modelAdmins.map { ma =>
    val html = generateSingleModelHtml(header, ma)

    pathPrefix(ma.modelView.modelName) {
      completeHtml(html)
    }
  }.reduce(_ ~ _)

  val route: Route = pathPrefix("admin") {
    pathEndOrSingleSlash(completeHtml(mainPageHtml)) ~ modelAdminRoutes
  } ~ respondWithHeaders(noCorsHeaders) {
    pathPrefix("api") {
      modelAdmins.map(_.route).reduce(_ ~ _)
    }
  } ~ pathPrefix("static")(getFromResourceDirectory("static"))

  def completeHtml(html: String): StandardRoute = {
    complete(
      HttpEntity(
        ContentTypes.`text/html(UTF-8)`,
        html
      )
    )
  }

  //Todo: Create auth for admin panel
}
