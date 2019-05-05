package scalest.admin

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{Directive0, Directives, Route}

trait CorsDirectives extends Directives {

  def corsSupport(f: => Route): Route = corsHeaders {
    preflightRoute ~ f
  }

  def preflightRoute: Route = options {
    complete(HttpResponse(StatusCodes.OK).withHeaders(`Access-Control-Allow-Methods`(OPTIONS, POST, PUT, GET, DELETE)))
  }

  def corsHeaders: Directive0 = respondWithHeaders(
    `Access-Control-Allow-Origin`(HttpOriginRange("http://localhost:8080")),
    `Access-Control-Allow-Methods`(OPTIONS, POST, PUT, GET, DELETE),
    `Access-Control-Allow-Credentials`(true),
    `Access-Control-Allow-Headers`("Authorization", "Content-Type", "X-Requested-With")
    )
}
