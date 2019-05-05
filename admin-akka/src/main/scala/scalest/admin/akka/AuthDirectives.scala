package scalest.admin.akka

import akka.http.scaladsl.model.StatusCodes.Unauthorized
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.Credentials
import scalest.admin.{ErrorResponse, AuthService}
import scalest.admin.AuthService.{AuthSuccess, CredentialsIncorrect, LoginRequest}

trait AuthDirectives extends AuthService with HttpService {
  def auth(f: => Route): Route = authenticateBasic(realm = "scalest admin", basicAuthenticator)(_ => f)

  private def basicAuthenticator(credentials: Credentials): Option[String] = {
    credentials match {
      case p@Credentials.Provided(username) if p.verify(AdminPassword) && username == AdminUsername => Some(username)
      case _                                                                                        => None
    }
  }

  def loginRoute: Route = {
    pathPrefix("login") {
      post {
        pathEndOrSingleSlash {
          entity(as[LoginRequest]) {
            case LoginRequest(AdminUsername, AdminPassword) => complete(AuthSuccess(Token))
            case _                                          => complete(Unauthorized, ErrorResponse(CredentialsIncorrect))
          }
        }
      }
    }
  }
}
