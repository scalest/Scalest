package scalest.admin

import scalatags.Text.all._
import scalest.admin.Vue._
import io.circe.syntax._
import ReflectUtils._
import scala.reflect.runtime.universe.TypeTag

trait ModelViewInstances {

  def parseField(parser: String => String = n => n)
                (implicit n: String) =
    s"""$n: ${parser(s"this.$editedItem")}"""

  def editedItem(implicit n: String) = s"editedItem.$n"

  def viewItem(implicit n: String) = s"props.item.$n"

  val intIdMV: ModelView = ModelView.instance(
    "0",
    _ => "",
    implicit n => s"{{ $viewItem }}",
    implicit n => parseField(item => s"parseInt($item)")
  )

  val strMV: ModelView = ModelView.instance(
    "\"\"",
    implicit n => vTextField(vModel := editedItem, attr("label") := n.capitalize).render,
    implicit n => s"{{ $viewItem }}",
    implicit n => parseField()
  )

  val boolMV: ModelView = ModelView.instance(
    "false",
    implicit n => vSwitch(vModel := editedItem, attr("label") := n.capitalize).render,
    implicit n => vIcon(vBind("color") := s"""$viewItem? "green": "red" """)(s"""{{ $viewItem ? "check_circle" : "cancel"}}""")
      .render,
    implicit n => parseField()
  )

  def enumMV[T <: Enumeration#Value : TypeTag]: ModelView = {
    val enum = getEnum[T]
    val enumName = enum.toString()
    val defaultValue = enum.values.head.toString.asJson.noSpaces
    val enumValues = enum.values.map(el => s""""$el"""").mkString("[", ",", "]")

    ModelView.instance(
      defaultValue,
      implicit n => {
        vSelect(
          vModel := editedItem,
          attr("label") := enumName,
          attr("solo"),
          vBind("items") := enumValues
        ).render
      },
      implicit n => s"{{ $viewItem }}",
      implicit n => parseField()
    )
  }

  //Todo: capture all core Field types
}

object ModelViewInstances
  extends ModelViewInstances

trait ModelView {
  def toInput(name: String): String

  def toOutput(name: String): String

  def parseForm(name: String): String

  def defaultValue(): String
}

object ModelView {
  def instance(defaultVal: String,
               toInputFunc: String => String,
               toOutputFunc: String => String,
               parseFormFunc: String => String): ModelView = new ModelView {
    override def toInput(name: String): String = toInputFunc(name)

    override def toOutput(name: String): String = toOutputFunc(name)

    override def parseForm(name: String): String = parseFormFunc(name)

    override def defaultValue(): String = defaultVal
  }
}
