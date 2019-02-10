package scalest.admin

import cats.Applicative
import io.circe.generic.extras
import io.circe.generic.extras.Configuration
import io.circe.generic.semiauto.deriveCodec
import io.circe.{Codec, Decoder, DecodingFailure, Json}
import scalest.admin.pagination.PageResponse
import scalest.admin.schema.ModelSchema

import scala.reflect.ClassTag

package object action {

  sealed trait ActionResponse {
    def pure[F[_]: Applicative]: F[ActionResponse] = Applicative[F].pure(this)
  }

  object ActionResponse {
    implicit val configuration: Configuration = Configuration.default.withDiscriminator("type")
    implicit val codec: Codec[ActionResponse] = extras.semiauto.deriveConfiguredCodec
  }

  case object ActionSuccess extends ActionResponse {
    implicit val codec: Codec[ActionSuccess.type] = deriveCodec
  }

  case object ActionNotFound extends ActionResponse {
    implicit val codec: Codec[ActionNotFound.type] = deriveCodec
  }

  case object ActionFailure extends ActionResponse {
    implicit val codec: Codec[ActionFailure.type] = deriveCodec
  }

  case class Action[F[_], Form](name: String)(handler: Form => F[ActionResponse])(
    implicit
    val schema: ModelSchema[Form],
    val decoder: Decoder[Form],
    val tag: ClassTag[Form],
  ) {
    def execute(data: Json): Either[DecodingFailure, F[ActionResponse]] = decoder.decodeJson(data).map(handler)
  }

  case class SearchAction[F[_], Model, Query](name: String)(handler: Query => F[PageResponse[Model]])(
    implicit
    val schema: ModelSchema[Query],
    val decoder: Decoder[Query],
    val tag: ClassTag[Query],
  ) {
    def execute(data: Json): Either[DecodingFailure, F[PageResponse[Model]]] = decoder.decodeJson(data).map(handler)
  }

  case class ActionRequest(name: String, data: Json)

  object ActionRequest {
    implicit val codec: Codec[ActionRequest] = deriveCodec
  }

  case class ActionSchema(name: String, schema: ModelSchema[_])

}
