package scalest.admin

import java.util.Base64

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

trait AuthService {
  val AdminUsername: String = "admin"

  val AdminPassword: String = "nimda"

  val Token: String = Base64.getEncoder.encodeToString(s"$AdminUsername:$AdminPassword".getBytes)
}

object AuthService {
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
