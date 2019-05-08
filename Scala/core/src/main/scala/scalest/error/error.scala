package scalest

import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredCodec
import io.circe.generic.semiauto.deriveCodec
import io.circe.{Codec, Json}

package object error extends ErrorMessages {
  implicit val configuration: Configuration = Configuration.default.withDiscriminator("type")

  sealed trait CommonError {
    def error: String
    def details: Option[Json]
  }

  object CommonError {
    implicit val codec: Codec[CommonError] = deriveConfiguredCodec[CommonError]
  }

  case class InternalServerError(error: String,
                                 details: Option[Json] = None) extends CommonError

  object InternalServerError {
    implicit val codec: Codec[InternalServerError] = deriveConfiguredCodec[InternalServerError]
  }

  case class ApplicationError(error: String,
                              details: Option[Json] = None) extends CommonError

  object ApplicationError {
    implicit val codec: Codec[ApplicationError] = deriveConfiguredCodec[ApplicationError]
  }

  case class NotFoundError(error: String,
                           details: Option[Json] = None) extends CommonError

  object NotFoundError {
    implicit val codec: Codec[NotFoundError] = deriveCodec[NotFoundError]
  }

  case class FieldValidationError(error: String,
                                  description: Option[String] = None)

  object FieldValidationError {
    implicit val codec: Codec[FieldValidationError] = deriveConfiguredCodec[FieldValidationError]
  }

  case class ModelValidationError(error: String,
                                  fields: Map[String, FieldValidationError] = Map.empty,
                                  details: Option[Json] = None) extends CommonError

  object ModelValidationError {
    implicit val codec: Codec[ModelValidationError] = deriveConfiguredCodec[ModelValidationError]
  }

}
