package scalest.admin

import cats.ApplicativeError
import cats.implicits._
import io.circe.Printer
import io.circe.syntax._
import scalest.error
import scalest.tapir._
import sttp.tapir.openapi.{Info, OpenAPI}
import sttp.tapir.server.ServerEndpoint

case class SwaggerModule[F[_]](info: Info,
                               endpoints: List[ServerEndpoint[_, _, _, _, F]])
                              (implicit C: ApplicativeError[F, Throwable]) extends TapirModule[F] {
  val printer: Printer = Printer.spaces2.copy(dropNullValues = true)
  val openApi: OpenAPI = endpoints.map(_.tag(info.title)).toOpenAPI(info)
  val yaml: String = openApi.toYaml
  val json: String = openApi.asJson.printWith(printer)

  override val routes: List[ServerEndpoint[_, _, _, Nothing, F]] =
    List(
      swaggerEndpoint("Swagger JSON", "swagger.json", json),
      swaggerEndpoint("Swagger YAML", "swagger.yaml", yaml),
      swaggerEndpoint("Swagger", "swagger", yaml)
    )

  def swaggerEndpoint(name: String, path: String, content: String): ServerEndpoint[Unit, error.CommonError, String, Nothing, F] = {
    commonEndpoint(name)
      .in("api" / path)
      .out(stringBody)
      .tapir(_ => content.pure[F])
  }
}