package scalest

import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.syntax._
import io.circe.generic.decoding.DerivedDecoder
import io.circe.generic.encoding.DerivedObjectEncoder
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import scalest.admin.ModelSchema
import shapeless.Lazy

import scala.concurrent.Future

package object admin extends FieldTypeSchemaInstances {

  abstract class ModelAction[Model, Id] {
    val name: String
    val handler: Set[Id] => Future[Seq[Model]]
  }

  case class ModelActionCommand[Id](name: String, ids: Set[Id])

  object ModelActionCommand {
    implicit def jc[Id: Encoder : Decoder]: JsonConverter[ModelActionCommand[Id]] = CirceHelpers.circeObject
  }

  case class ErrorResponse(error: String)

  object ErrorResponse {
    implicit val encoder: Encoder[ErrorResponse] = deriveEncoder
  }

  case class ModelInfo(schema: ModelSchema[_], actions: Set[String] = Set.empty)

  object ModelInfo {
    implicit val encoder: Encoder[ModelInfo] = Encoder.instance{ ma =>
      import ma._
      Json.obj("actions" -> actions.asJson, "schema" -> ModelSchema.encoder(schema))
    }
  }
}

case class JsonConverter[T](encoder: Encoder[T], decoder: Decoder[T]) extends Encoder[T] with Decoder[T] {

  override def apply(a: T): Json = encoder.apply(a)

  override def apply(c: HCursor): Result[T] = decoder.apply(c)
}

trait CirceHelpers {
  def circeObject[T](implicit decode: Lazy[DerivedDecoder[T]],
                     encode: Lazy[DerivedObjectEncoder[T]]): JsonConverter[T] = JsonConverter(deriveEncoder[T], deriveDecoder[T])

  def circeEnum[E <: Enumeration](enum: E): JsonConverter[E#Value] = JsonConverter(Encoder.enumEncoder(enum), Decoder.enumDecoder(enum))
}

object CirceHelpers extends CirceHelpers
