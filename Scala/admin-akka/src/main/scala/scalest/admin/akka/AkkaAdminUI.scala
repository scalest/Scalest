package scalest.admin.akka

import akka.http.scaladsl.server.{Directives, Route}

object AkkaAdminUI extends Directives {
  def apply(): Route = indexRoute ~ staticRoute
  def indexRoute: Route =
    staticResource("admin", "admin-ui/index.html") ~ staticResource("scalest_logo", "scalest_logo.png")
  def staticRoute: Route = adminStaticRoute ~ miscStaticRoute
  def miscStaticRoute: Route = staticResources("favicon.ico", "scalest.png", "material.css")
  def adminStaticRoute: Route = pathPrefix("static")(getFromResourceDirectory("admin-ui/static"))
  def staticResources(names: String*): Route =
    names.map(n => staticResource(n, s"admin-ui/$n")).foldLeft[Route](reject)(_ ~ _)
  def staticResource(prefix: String, resource: String): Route =
    pathPrefix(prefix)(pathEndOrSingleSlash(getFromResource(resource)))
}
