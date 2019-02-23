package scalest.admin

import io.circe.syntax._
import scalatags.Text.all._
import scalest.admin.Utils._
import scalest.admin.Vue._

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.reflect.runtime.universe.TypeTag


case class FieldView(name: String,
                     ftv: FieldTypeView[_],
                     writeable: Boolean = true,
                     readable: Boolean = true,
                     default: Option[String] = None,
                     parse: Option[String => String] = None) {
  def toInput(): Frag = if (writeable) raw(ftv.toInput(name)) else emptyFrag

  def toOutput(): Frag = if (readable) td(raw(ftv.toOutput(name))) else td

  def parseForm(): String = parse.getOrElse(ftv.parseForm _)(name)

  def defaultValue(): String = default.getOrElse(ftv.defaultValue())
}

class NoRead
  extends StaticAnnotation

class NoWrite
  extends StaticAnnotation

final case class DefaultValue(default: String)
  extends StaticAnnotation

final case class FormParse(parse: String => String)
  extends StaticAnnotation


trait FieldTypeView[T] {
  def toInput(name: String): String

  def toOutput(name: String): String

  def parseForm(name: String): String

  def defaultValue(): String
}

object FieldTypeView {

  def instance[T](default: String,
                  input: String => String,
                  output: String => String,
                  parse: String => String): FieldTypeView[T] = new FieldTypeView[T] {
    override def toInput(name: String): String = input(name)

    override def toOutput(name: String): String = output(name)

    override def parseForm(name: String): String = parse(name)

    override def defaultValue(): String = default
  }
}

trait FieldTypeViewInstances {

  def parseField(parser: String => String = n => n)
                (implicit n: String) =
    s"""$n: ${parser(s"this.$editedItem")}"""

  def editedItem(implicit n: String) = s"editedItem.$n"

  def viewItem(implicit n: String) = s"props.item.$n"

  implicit val intFTV: FieldTypeView[Int] = FieldTypeView.instance(
    "0",
    implicit n => vTextField(vModel := editedItem, `type` := "number", attr("label") := n.capitalize).render,
    implicit n => s"{{ $viewItem }}",
    implicit n => parseField(item => s"parseInt($item)")
  )

  implicit val strFTV: FieldTypeView[String] = FieldTypeView.instance(
    "\"\"",
    implicit n => vTextField(vModel := editedItem, attr("label") := n.capitalize).render,
    implicit n => s"{{ $viewItem }}",
    implicit n => parseField()
  )

  implicit val boolFTV: FieldTypeView[Boolean] = FieldTypeView.instance(
    "false",
    implicit n => vSwitch(vModel := editedItem, attr("label") := n.capitalize).render,
    implicit n => vIcon(vBind("color") := s"""$viewItem? "green": "red" """)(s"""{{ $viewItem ? "check_circle" : "cancel"}}""")
      .render,
    implicit n => parseField()
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
      implicit n => {
        vSelect(
          vModel := editedItem,
          attr("label") := enumName,
          attr("solo"),
          vBind("items") := enumValues
        ).render
      },
      implicit n => tag("v-chip")(`class` := "primary", attr("text-color") := "white")(s"{{ $viewItem }}").render,
      implicit n => parseField()
    )
  }

  //Todo: capture all core Field types
}