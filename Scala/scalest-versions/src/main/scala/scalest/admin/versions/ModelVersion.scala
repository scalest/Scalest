package scalest.admin.versions

import java.time.LocalDateTime

import io.circe.generic.semiauto.deriveCodec
import io.circe.{Codec, Json}
import scalest.admin.schema.ModelSchema
import scalest.admin.versions.ModelActions.ModelAction
import scalest.tapir._

object ModelActions extends Enumeration {
  type ModelAction = Value
  val Create: ModelAction = Value("Create")
  val Update: ModelAction = Value("Update")
  val Delete: ModelAction = Value("Delete")
  implicit val codec: Codec[ModelAction] = Codec.codecForEnumeration(this)
}

case class ModelVersion(
  id: String,
  model: String,
  action: ModelAction,
  content: Json,
  author: String,
  creationDate: LocalDateTime = LocalDateTime.now(),
)

object ModelVersion {
  implicit val codec: Codec[ModelVersion] = deriveCodec
  implicit val modelSchema: ModelSchema[ModelVersion] = ModelSchema.gen[ModelVersion]
  implicit val modelED: EntityDescriptors[ModelVersion] = EntityDescriptors.create
}
