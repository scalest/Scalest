package scalest.admin

import magnolia.{CaseClass, Magnolia}
import scalest.admin.Utils._

import scala.language.experimental.macros

case class ModelView[T](modelName: String, modelRepr: ModelRepr)

object ModelView
  extends FieldTypeViewInstances {

  implicit def gen[T]: ModelView[T] = macro Magnolia.gen[T]

  type Typeclass[T] = FieldTypeView[T]

  def combine[T](ctx: CaseClass[FieldTypeView, T]): ModelView[T] = {
    new ModelView(
      Utils.snakify(ctx.typeName.short),
      ctx.parameters.map { p =>
        FieldView(
          name = p.label,
          ftv = p.typeclass,
          writeable = p.annotations.findOfType[NoWrite].forall(_ => false),
          readable = p.annotations.findOfType[NoRead].forall(_ => false),
          default = p.annotations.findOfType[DefaultValue].map(_.default),
          parse = p.annotations.findOfType[FormParse].map(_.parse)
        )
      }
    )
  }

}