package scalest.tapir

import scalest.error.{ApplicationError, CommonError, FieldValidationError, InternalErrorMessage, InternalServerError, MalformedFieldErrorMessage, ModelValidationError, ValidationErrorMessage}
import scalest.exception.{ApplicationException, DatabaseException, EntityNotFoundException, ModelValidationException}
import sttp.model.StatusCode
import sttp.tapir.server.DecodeFailureHandling.response
import sttp.tapir.server.{DecodeFailureHandler, DecodeFailureHandling, DefaultDecodeFailureHandler, ServerDefaults}
import sttp.tapir.{EndpointIO, EndpointInput, EndpointOutput}

trait TapirServerCommon { self: TapirJsonCirce =>
  def defaultFailureResponse(code: StatusCode, message: String): DecodeFailureHandling = {
    val out: EndpointOutput[ApplicationError] = statusCode(code).and(jsonBody[ApplicationError])
    response(out)(ApplicationError(message))
  }
  val defaultHandler: DefaultDecodeFailureHandler = ServerDefaults.decodeFailureHandler.copy(response = defaultFailureResponse)

  implicit val defaultErrorHandler: EffectErrorHandler[CommonError] = {
    case e: EntityNotFoundException  => e.error
    case e: ApplicationException     => e.error
    case e: ModelValidationException => e.error
    case e: DatabaseException        => e.error
    case _                           => InternalServerError(InternalErrorMessage)
  }

  val decodeFailureHandler: DecodeFailureHandler = ctx => {
    ctx.input match {
      case h: EndpointInput.Query[_]   =>
        response(jsonBody[ModelValidationError])(ModelValidationError(ValidationErrorMessage, Map(h.name -> FieldValidationError(MalformedFieldErrorMessage))))
      case _: EndpointIO.Body[_, _, _] =>
        val decodeError = ctx.failure match {
          case sttp.tapir.DecodeResult.Error(_, failure: DecodeException) => failure.toModelValidationError
          case _                                                          => ModelValidationError(ValidationErrorMessage)
        }
        response(jsonBody[ModelValidationError])(decodeError)

      case _ => defaultHandler(ctx)
    }
  }

}
