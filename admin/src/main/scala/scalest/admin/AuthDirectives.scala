package scalest.admin

import java.util.Base64

import akka.http.scaladsl.model.StatusCodes.Unauthorized
import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.server.directives.Credentials
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import scalest.admin.AdminExtension.AppError
import scalest.admin.AuthDirectives.{AuthSuccess, CredentialsIncorrect, LoginRequest}

trait AuthDirectives extends Directives with ErrorAccumulatingCirceSupport {
  private val AdminUsername: String = "admin"

  private val AdminPassword: String = "nimda"

  private val Token = Base64.getEncoder.encodeToString(s"$AdminUsername:$AdminPassword".getBytes)

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
            case _                                          => complete(Unauthorized, AppError(CredentialsIncorrect))
          }
        }
      }
    }
  }
}

object AuthDirectives {
  val CredentialsIncorrect = "error.credentials.incorrect"

  final case class LoginRequest(username: String, password: String)

  object LoginRequest {
    implicit val decoder: Decoder[LoginRequest] = deriveDecoder
  }

  case class AuthSuccess(token: String)

  object AuthSuccess {
    implicit val encoder: Encoder[AuthSuccess] = deriveEncoder
  }

}
