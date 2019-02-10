package scalest.admin.schema

import magnolia.{debug, CaseClass, Magnolia, SealedTrait}
import scalest.admin.dto._

import scala.language.experimental.macros
import io.scalaland.chimney.dsl.TransformerOps

case class ModelSchema[T](name: String, fields: Seq[FieldViewDto]) {
  def dto: ModelSchemaDto = this.transformInto[ModelSchemaDto]
}

object ModelSchema {
  def apply[T](implicit MS: ModelSchema[T]): ModelSchema[T] = MS
  type Typeclass[T] = FieldSchema[T]

  @debug implicit def gen[T]: ModelSchema[T] = macro Magnolia.gen[T]

  def combine[T](ctx: CaseClass[FieldSchema, T]): ModelSchema[T] =
    ModelSchema(
      ctx.typeName.short,
      ctx.parameters.map(p => FieldViewDto(name = p.label, schema = p.typeclass.toDto)),
    )

  def snakify(name: String): String =
    name.replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2").replaceAll("([a-z\\d])([A-Z])", "$1_$2").toLowerCase
}
