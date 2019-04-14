package scalest

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

package object admin extends FieldTypeSchemaInstances {

  final case class LoginRequest(username: String, password: String)

  object LoginRequest {
    implicit val decoder: Decoder[LoginRequest] = deriveDecoder
  }

  case class AuthSuccess(token: String)

  object AuthSuccess {
    implicit val encoder: Encoder[AuthSuccess] = deriveEncoder
  }

  case class AppError(error: String)

  object AppError {
    implicit val encoder: Encoder[AppError] = deriveEncoder
  }

}
