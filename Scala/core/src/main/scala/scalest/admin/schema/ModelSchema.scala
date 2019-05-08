package scalest.admin.schema

import magnolia.{debug, CaseClass, Magnolia}
import scalest.admin.dto._

import scala.language.experimental.macros
import io.circe.Encoder
import io.circe.Decoder

case class ModelSchema[T](name: String, fields: Seq[FieldViewDto])

object ModelSchema {
  type Typeclass[T] = FieldSchema[T]

  @debug implicit def gen[T]: ModelSchema[T] = macro Magnolia.gen[T]

  def combine[T](ctx: CaseClass[FieldSchema, T]): ModelSchema[T] =
    ModelSchema(
      snakify(ctx.typeName.short),
      ctx.parameters.map(p => FieldViewDto(name = p.label, schema = p.typeclass.toDto))
    )

  def snakify(name: String): String =
    name.replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2").replaceAll("([a-z\\d])([A-Z])", "$1_$2").toLowerCase
}
