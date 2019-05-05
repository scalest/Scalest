package scalest.admin.akka

import akka.http.scaladsl.server.Directives
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import scalest.CirceHelpers

trait HttpService extends Directives with ErrorAccumulatingCirceSupport with CirceHelpers
