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


  //Todo: Map, Json, Markdown, components
  //Todo: Validation

  implicit val intFTV: FieldTypeView[Int] = FieldTypeView.instance(
    "0",
    meta => vTextField(vModel := meta.accessorName, attr("solo"), `type` := "number", attr("label") := meta.fieldName.capitalize).render,
    meta => s"{{ ${meta.accessorName} }}",
    meta => parseField(meta, item => s"parseInt($item)")
  )

  //Should get correct validation
  implicit val shortFTV: FieldTypeView[Short] = intFTV.copy

  implicit val longFTV: FieldTypeView[Char] = intFTV.copy

  implicit val byteFTV: FieldTypeView[Byte] = intFTV.copy

  implicit val charFTV: FieldTypeView[Char] = intFTV.copy

  implicit val floatFTV: FieldTypeView[Float] = intFTV.copy

  implicit val doubleFTV: FieldTypeView[Double] = intFTV.copy

  implicit val bigDecimalFTV: FieldTypeView[BigDecimal] = intFTV.copy

  implicit val bigIntFTV: FieldTypeView[BigInt] = intFTV.copy

  implicit val strFTV: FieldTypeView[String] = FieldTypeView.instance(
    "\"\"",
    meta => vTextField(vModel := meta.accessorName, attr("solo"), attr("label") := meta.fieldName.capitalize).render,
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

  implicit def seqFTV[T](implicit elementFTV: FieldTypeView[T]): FieldTypeView[Seq[T]] = FieldTypeView.instance(
    default = "[]",
    meta => vCard(`class` := "mb-4")(
      vCardTitle(
        span(`class` := "headline")(meta.fieldName),
        vSpacer,
        vBtn(`@click` := s"${meta.accessorName}.unshift(${elementFTV.defaultValue()})", attr("color") := "blue", `class` := "white--text")(
          "Create"
        )
      ),
      vCardText(
        vList(
          vTemplate(vIf := s"${meta.accessorName}.length <= 10", vFor := s"(item, index) in ${meta.accessorName}")(
            vListTile(vBind("key") := "index", `class` := "py-1")(
              tag("v-list-tile-content")(style := "overflow: inherit; display: unset;")(
                raw(elementFTV.toInput(FieldMeta("", s"${meta.accessorName}[index]")))
              ),
              tag("v-list-tile-action")(
                vBtn(attr("icon"), attr("flat"), attr("color") := "red lighten-3", `@click` := s"${meta.accessorName}.splice(index, 1)")(
                  vIcon("close")
                )
              )
            )
          ),
          tag("virtual-list")(vIf := s"${meta.accessorName}.length > 10", vBind("size") := "40", vBind("remain") := "10")(
            vTemplate(vFor := s"(item, index) in ${meta.accessorName}")(
              vListTile(`class` := "item py-1", vBind("key") := "index")(
                tag("v-list-tile-content")(style := "overflow: inherit; display: unset;")(
                  raw(elementFTV.toInput(FieldMeta("", s"${meta.accessorName}[index]")))
                ),
                tag("v-list-tile-action")(
                  vBtn(attr("icon"), attr("flat"), attr("color") := "red lighten-3", `@click` := s"${meta.accessorName}.splice(index, 1)")(
                    vIcon("close")
                  )
                )
              )
            )
          )
        )
      )
    ).render,
    meta => vMenu(attr("offset-y"))(
      vToolbarTitle(attr("slot") := "activator")(
        vBtn(attr("dark"), attr("color") := "indigo", attr("fab"), attr("small"))(
          vIcon(attr("dark"))("visibility")
        )
      ),
      vList(
        vTemplate(vIf := s"${meta.accessorName}.length <= 10", vFor := s"(item, index) in ${meta.accessorName}")(
          vListTile(vBind("key") := "index")(
            elementFTV.toOutput(FieldMeta("index", "item"))
          )
        ),
        tag("virtual-list")(vIf := s"${meta.accessorName}.length > 10", vBind("size") := "40", vBind("remain") := "10")(
          vTemplate(vFor := s"(item, index) in ${meta.accessorName}")(
            vListTile(`class` := "item", vBind("key") := "index")(
              elementFTV.toOutput(FieldMeta("index", "item"))
            )
          )
        )
      )
    ).render,
    meta => parseField(meta)
  )

  implicit def listFTV[T](implicit elementFTV: FieldTypeView[T]): FieldTypeView[List[T]] = seqFTV[T].copy

  implicit def setFTV[T](implicit elementFTV: FieldTypeView[T]): FieldTypeView[Set[T]] = seqFTV[T].copy

  implicit def arrayFTV[T](implicit elementFTV: FieldTypeView[T]): FieldTypeView[Array[T]] = seqFTV[T].copy

  //This maybe should do something -__-
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
}