package scalest.admin

import java.util.Base64

import akka.http.scaladsl.model.StatusCodes.Unauthorized
import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.server.directives.Credentials
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport

trait AuthDirectives extends Directives with ErrorAccumulatingCirceSupport {
  private val ADMIN_USERNAME: String = "admin"

  private val ADMIN_PASSWORD: String = "nimda"

  def auth(f: => Route): Route = authenticateBasic(realm = "scalest admin", basicAuthenticator)(_ => f)

  private def basicAuthenticator(credentials: Credentials): Option[String] = {
    credentials match {
      case p@Credentials.Provided(username) if p.verify(ADMIN_PASSWORD) && username == ADMIN_USERNAME => Some(username)
      case _                                                                                          => None
    }
  }

  def loginRoute: Route =
    pathPrefix("login") {
      post {
        pathEndOrSingleSlash {
          entity(as[LoginRequest]) { case request => import request._
            val credentialsMatch = username == ADMIN_USERNAME && password == ADMIN_PASSWORD

            if (credentialsMatch) {
              val token = Base64.getEncoder.encodeToString(s"$username:$password".getBytes)

              complete(AuthSuccess(token))
            } else {
              complete(Unauthorized, AppError("error.credentials.incorrect"))
            }
          }
        }
      }
    }
}
