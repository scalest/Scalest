package scalest

import io.circe.generic.semiauto._
import io.circe.{Codec, Decoder, Encoder}
import scalest.admin.schema.FieldTypeSchemaInstances

package object admin extends FieldTypeSchemaInstances {
  val ErrorActionUnhandled: String = "error.action.not-handled"

  case class ModelAction[F[_], Model, Id](name: String)(val handler: Set[Id] => F[Seq[Model]])

  case class ModelActionCommand[Id](name: String, ids: Set[Id])

  object ModelActionCommand {
    implicit def jc[Id: Encoder: Decoder]: Codec[ModelActionCommand[Id]] = deriveCodec
  }

}
