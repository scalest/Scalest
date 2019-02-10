package scalest.tapir

import cats.implicits._
import io.circe.CursorOp
import scalest.error.{FieldValidationError, MalformedFieldErrorMessage, ModelValidationError, ValidationErrorMessage}

import scala.util.control.NoStackTrace

case class DecodeException(failures: List[io.circe.DecodingFailure] = List.empty) extends Throwable with NoStackTrace {
  def toModelValidationError: ModelValidationError = {
    val fieldErrors = failures
      .map(failure => CursorOp.opsToPath(failure.history).some.filter(_.nonEmpty).map(_.substring(1)).getOrElse("."))
      .map(path => path -> FieldValidationError(MalformedFieldErrorMessage))
      .toMap

    ModelValidationError(ValidationErrorMessage, fieldErrors)
  }
}
