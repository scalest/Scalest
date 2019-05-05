package scalest.admin

import io.circe.Encoder.encodeSeq
import io.circe._
import io.circe.syntax._
import magnolia.{CaseClass, Magnolia, debug}

import scala.language.experimental.macros

case class FieldView[T](name: String,
                        schema: FieldSchema[T],
                        readable: Boolean = true,
                        writable: Boolean = true)

object FieldView {
  implicit val encoder: Encoder[FieldView[_]] = Encoder.instance { fw =>
    Json.obj("name" -> fw.name.asJson, "readable" -> fw.readable.asJson, "writable" -> fw.writable.asJson, "schema" -> fw.schema.asJson)
  }
}

case class ModelSchema[T](name: String, fields: Seq[FieldView[_]])

object ModelSchema extends FieldTypeSchemaInstances {

  def apply[T](conf: (String => _, FieldView[_])*)(implicit ms: ModelSchema[T]): ModelSchema[T] = {
    ms
  }

  type Typeclass[T] = FieldSchema[T]

  @debug implicit def gen[T]: ModelSchema[T] = macro Magnolia.gen[T]

  def combine[T](ctx: CaseClass[FieldSchema, T]): ModelSchema[T] = {
    ModelSchema(Utils.snakify(ctx.typeName.short), ctx.parameters.map(p => FieldView(name = p.label, schema = p.typeclass)))
  }

  implicit val encoder: Encoder[ModelSchema[_]] = Encoder.instance { ms =>
    Json.obj("name" -> ms.name.asJson, "fields" -> ms.fields.asJson(encodeSeq(FieldView.encoder)))
  }
}