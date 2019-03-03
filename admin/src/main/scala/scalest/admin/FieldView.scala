package scalest.admin

import io.circe.syntax._
import scalatags.Text.all._
import scalest.admin.Utils._
import scalest.admin.Vue._

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.reflect.runtime.universe.TypeTag


case class FieldMeta(fieldName: String, accessorName: String)

case class FieldView(name: String,
                     ftv: FieldTypeView[_],
                     writeable: Boolean = true,
                     readable: Boolean = true,
                     default: Option[String] = None,
                     parse: Option[FieldMeta => String] = None) {
  def toInput(): Frag = if (writeable) raw(ftv.toInput(FieldMeta(name, s"editedItem.$name"))) else emptyFrag

  def toOutput(): Frag = if (readable) td(raw(ftv.toOutput(FieldMeta(name, s"props.item.$name")))) else td

  def parseForm(): String = parse.getOrElse(ftv.parseForm _)(FieldMeta(name, s"this.editedItem.$name"))

  def defaultValue(): String = default.getOrElse(ftv.defaultValue())
}

class NoRead
  extends StaticAnnotation

class NoWrite
  extends StaticAnnotation

final case class DefaultValue(default: String)
  extends StaticAnnotation

final case class FormParse(parse: FieldMeta => String)
  extends StaticAnnotation


trait FieldTypeView[T] {
  def toInput(meta: FieldMeta): String

  def toOutput(meta: FieldMeta): String

  def parseForm(meta: FieldMeta): String

  def defaultValue(): String

  def polymap[Other](default: String = defaultValue(),
                     input: FieldMeta => String = toInput,
                     output: FieldMeta => String = toOutput,
                     parse: FieldMeta => String = parseForm): FieldTypeView[Other] = FieldTypeView.instance(
    default,
    input,
    output,
    parse
  )

  def copy[Other]: FieldTypeView[Other] = FieldTypeView.instance(defaultValue(), toInput, toOutput, parseForm)
}

object FieldTypeView {

  def instance[T](default: String,
                  input: FieldMeta => String,
                  output: FieldMeta => String,
                  parse: FieldMeta => String): FieldTypeView[T] = new FieldTypeView[T] {
    override def toInput(meta: FieldMeta): String = input(meta)

    override def toOutput(meta: FieldMeta): String = output(meta)

    override def parseForm(meta: FieldMeta): String = parse(meta)

    override def defaultValue(): String = default
  }
}

trait FieldTypeViewInstances {

  def parseField(meta: FieldMeta, parser: String => String = n => n) =
    s"""${meta.fieldName}: ${parser(meta.accessorName)}"""


  implicit val intFTV: FieldTypeView[Int] = FieldTypeView.instance(
    "0",
    meta => vTextField(vModel := meta.accessorName, `type` := "number", attr("label") := meta.fieldName.capitalize).render,
    meta => s"{{ ${meta.accessorName} }}",
    meta => parseField(meta, item => s"parseInt($item)")
  )


  implicit def seqFTV[T](implicit elementFTV: FieldTypeView[T]): FieldTypeView[Seq[T]] = FieldTypeView.instance(
    default = "[]",
    meta => "",
    meta => vMenu(attr("offset-y"))(
      vToolbarTitle(attr("slot") := "activator")(
        span(meta.fieldName.capitalize),
        vIcon("arrow_drop_down")
      ),
      vList(
        vListTile(vFor := "(item, index) in items", vBind("key") := "index")(
          elementFTV.toOutput(FieldMeta("index", "item"))
        )
      )
    ).render,
    meta => parseField(meta)
  )

  implicit val shortFTV: FieldTypeView[Short] = intFTV.copy

  implicit val longFTV: FieldTypeView[Char] = intFTV.copy

  implicit val byteFTV: FieldTypeView[Byte] = intFTV.copy

  implicit val charFTV: FieldTypeView[Char] = intFTV.copy

  implicit val strFTV: FieldTypeView[String] = FieldTypeView.instance(
    "\"\"",
    meta => vTextField(vModel := meta.accessorName, attr("label") := meta.fieldName.capitalize).render,
    meta => s"{{ ${meta.accessorName} }}",
    meta => parseField(meta)
  )

  implicit val boolFTV: FieldTypeView[Boolean] = FieldTypeView.instance(
    "false",
    meta => vSwitch(vModel := meta.accessorName, attr("label") := meta.fieldName.capitalize).render,
    meta => vIcon(vBind("color") := s"""${meta.accessorName}? "green": "red" """)(
      s"""{{ ${
        meta
          .accessorName
      } ? "check_circle" : "cancel"}}"""
    )
      .render,
    meta => parseField(meta)
  )

  implicit def optionFTV[T](implicit fieldView: FieldTypeView[T]): FieldTypeView[Option[T]] =
    FieldTypeView.instance(
      fieldView.defaultValue(),
      fieldView.toInput,
      fieldView.toOutput,
      fieldView.parseForm
    )

  implicit def enumFTV[T <: Enumeration#Value : TypeTag]: FieldTypeView[T] = {
    val enum = getEnum[T]
    val enumName = enum.toString()
    val defaultValue = enum.values.head.toString.asJson.noSpaces
    val enumValues = enum.values.map(el => s""""$el"""").mkString("[", ",", "]")

    FieldTypeView.instance(
      defaultValue,
      meta => {
        vSelect(
          vModel := meta.accessorName,
          attr("label") := enumName,
          attr("solo"),
          vBind("items") := enumValues
        ).render
      },
      meta => tag("v-chip")(`class` := "primary", attr("text-color") := "white")(s"{{ ${meta.accessorName} }}").render,
      meta => parseField(meta)
    )
  }

  //Todo: capture all core Field types
}