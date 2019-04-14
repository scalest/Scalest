package scalest.admin

import io.circe.{Encoder, Json}
import io.circe.syntax._
import magnolia.{CaseClass, Magnolia, SealedTrait, debug}

import scala.language.experimental.macros

case class FieldView(name: String, schema: FieldTypeSchema[_])

object FieldView {
  implicit val encoder: Encoder[FieldView] = Encoder.instance { fw =>
    Json.fromFields(
      Seq(
        "name" -> fw.name.asJson,
        "schema" -> fw.schema.asJson
        )
      )
  }
}

case class ModelSchema[T](name: String, fields: Seq[FieldView])

object ModelSchema extends FieldTypeSchemaInstances {

  type Typeclass[T] = FieldTypeSchema[T]

  @debug implicit def gen[T]: ModelSchema[T] = macro Magnolia.gen[T]

  def combine[T](ctx: CaseClass[FieldTypeSchema, T]): ModelSchema[T] = {
    ModelSchema(Utils.snakify(ctx.typeName.short), ctx.parameters.map(p => FieldView(name = p.label, schema = p.typeclass)))
  }

  implicit val encoder: Encoder[ModelSchema[_]] = Encoder.instance(ms => Json.fromFields(Seq("name" -> ms.name.asJson, "fields" -> ms.fields.asJson)))
}