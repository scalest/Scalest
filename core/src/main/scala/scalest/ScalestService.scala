package scalest

import akka.http.scaladsl.server.{Directives, Route}

trait ScalestService
  extends Directives {
  def route: Route
}
