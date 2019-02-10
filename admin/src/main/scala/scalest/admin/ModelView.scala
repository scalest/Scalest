package scalest.admin

import scalatags.Text.all._
import scalest.admin.Vue._

trait ModelViewInstances {
  val intIdMV: ModelView = ModelView.instance(
    "0",
    _ => "",
    n => td(s"{{ props.item.$n }}").render,
    n => s"""$n: parseInt(this.editedItem.$n)"""
  )

  val strMV: ModelView = ModelView.instance(
    "\"\"",
    n => vTextField(vModel := s"editedItem.$n", attr("label") := n.capitalize).render,
    n => td(s"{{ props.item.$n }}").render,
    n => s"""$n: this.editedItem.$n"""
  )

  val boolMV: ModelView = ModelView.instance(
    "false",
    n => vSwitch(vModel := s"editedItem.$n", attr("label") := n.capitalize).render,
    n => td(vCheckbox(vModel := s"props.item.$n", disabled)).render,
    n => s"""$n: this.editedItem.$n"""
  )
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
