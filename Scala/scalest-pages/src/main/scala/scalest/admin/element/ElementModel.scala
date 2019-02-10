package scalest.admin.element
import io.circe.Codec
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredCodec
import scalest.admin.schema.ModelSchema
import scalest.tapir.EntityDescriptors

object ElementModel {
  implicit val configuration: Configuration = Configuration.default.withDiscriminator("$type")

  case class Element(
    id: String,
    name: String,
    content: String,
  )

  object Element {
    implicit val codec: Codec[Element] = deriveConfiguredCodec
    implicit val modelSchema: ModelSchema[Element] = ModelSchema.gen
    implicit val modelED: EntityDescriptors[Element] = EntityDescriptors.create
  }

//  case class HtmlElement(
//    id: String,
//    name: String,
//    content: String,
//  ) extends Element
//
//  case class ListElement(
//    id: String,
//    name: String,
//    model: String,
//    varname: String,
//    child: Seq[Element],
//  ) extends Element
//
//  case class CondElement(
//    id: String,
//    name: String,
//    condition: String,
//    child: String,
//  )

}
