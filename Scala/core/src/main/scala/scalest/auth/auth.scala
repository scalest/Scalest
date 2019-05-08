package scalest

import java.util.Base64

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

package object auth {
  val AdminUsername: String = "admin"
  val AdminPassword: String = "nimda"
  val Token: String = Base64.getEncoder.encodeToString(s"$AdminUsername:$AdminPassword".getBytes)
  val CredentialsIncorrect = "error.credentials.incorrect"

  final case class AuthRequest(username: String, password: String)

  object AuthRequest {
    implicit val codec: Codec[AuthRequest] = deriveCodec
  }

  case class AuthResponse(token: String)

  object AuthResponse {
    implicit val codec: Codec[AuthResponse] = deriveCodec
  }

}
