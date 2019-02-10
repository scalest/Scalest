package scalest.admin

import io.circe.generic.semiauto
import io.circe.{Codec, Json}

package object dto {

  case class FieldSchemaDto(
    inputType: Option[String] = None,
    outputType: Option[String] = None,
    default: Option[Json] = None,
    addition: Option[Json] = None,
  )

  object FieldSchemaDto {
    implicit val codec: Codec[FieldSchemaDto] = semiauto.deriveCodec
  }

  case class FieldViewDto(name: String, schema: FieldSchemaDto, readable: Boolean = true, writable: Boolean = true)

  object FieldViewDto {
    implicit val codec: Codec[FieldViewDto] = semiauto.deriveCodec
  }

  case class ModelSchemaDto(name: String, fields: Seq[FieldViewDto])

  object ModelSchemaDto {
    implicit val codec: Codec[ModelSchemaDto] = semiauto.deriveCodec
  }

  case class ModelProtocol(schema: ModelSchemaDto, actions: List[ModelSchemaDto], searchActions: List[ModelSchemaDto])

  object ModelProtocol {
    implicit val codec: Codec[ModelProtocol] = semiauto.deriveCodec
  }

}
