package scalest.admin.akka

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}

package object cors {
  def corsWrap(f: => Route): Route = corsHeaders {
    preflightRoute ~ f
  }

  def preflightRoute: Route = options {
    complete(HttpResponse(OK))
  }

  def corsHeaders: Directive0 = respondWithHeaders(
    `Access-Control-Allow-Origin`.*,
    `Access-Control-Allow-Methods`(OPTIONS, POST, PUT, GET, DELETE),
    `Access-Control-Allow-Credentials`(true),
    `Access-Control-Allow-Headers`("Authorization", "Content-Type", "X-Requested-With"),
  )
}
