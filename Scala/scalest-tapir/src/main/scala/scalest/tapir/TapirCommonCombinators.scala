package scalest.tapir

import scalest.error.{CommonError, InternalServerError, NotFoundError}
import sttp.model.StatusCode
import sttp.tapir.{Codec, Endpoint, EndpointInput, EndpointOutput, Tapir, Validator}

import scala.language.experimental.macros

trait TapirCommonCombinators { self: Tapir with TapirJsonCirce with TapirJwtAuth =>
  def commonError(): EndpointOutput.OneOf[CommonError] =
    oneOf(
      statusMapping(StatusCode.NotFound, jsonBody[NotFoundError]),
      statusMapping(StatusCode.InternalServerError, jsonBody[InternalServerError]),
      statusDefaultMapping(jsonBody[CommonError]),
    )

  def uuidPath(name: String, prefix: String = ""): EndpointInput.PathCapture[String] = {
    val uuidCodec = Codec.stringPlainCodecUtf8.validate(Validator.pattern(s"($prefix(?:.{36}|.{32})(?:-test)*)"))
    path[String](name)(uuidCodec)
  }

  def commonEndpoint(name: String, description: String = ""): Endpoint[Unit, CommonError, Unit, Nothing] =
    endpoint
      .name(name)
      .description(description)
      .errorOut(commonError())

  object TapirCaseQuery {
    def apply[T]: EndpointInput[T] = macro TapirCaseQueryMacros.impl[T]
  }

  def caseQuery[T]: EndpointInput[T] = macro TapirCaseQueryMacros.impl[T]

}
