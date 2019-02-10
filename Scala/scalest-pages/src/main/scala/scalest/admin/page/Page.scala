package scalest.admin.page

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import scalest.admin.schema.ModelSchema
import scalest.auth.Permission
import scalest.tapir.EntityDescriptors

case class Page(
  id: String,
  path: String,
  elements: Seq[String],
  permission: Option[Permission],
)

object Page {
  implicit val codec: Codec[Page] = deriveCodec
  implicit val modelSchema: ModelSchema[Page] = ModelSchema.gen
  implicit val modelED: EntityDescriptors[Page] = EntityDescriptors.create
}
