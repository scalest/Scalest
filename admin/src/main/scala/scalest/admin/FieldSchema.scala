package scalest.admin

import io.circe.generic.semiauto._
import io.circe.syntax._
import io.circe.{Encoder, Json}
import scalest.admin.Utils._

import scala.language.experimental.macros
import scala.language.implicitConversions
import scala.reflect.runtime.universe.TypeTag

case class Component(id: String, body: String)

object Component {
  def apply(id: String, body: String): Component = new Component(id, s"const body = $body;body;")

  implicit val encoder: Encoder[Component] = deriveEncoder[Component]
}

case class FieldSchema[T](inputType: Option[String] = None,
                          outputType: Option[String] = None,
                          inputComponent: Option[Component] = None,
                          outputComponent: Option[Component] = None,
                          default: Option[Json] = None,
                          addition: Option[Json] = None)

object FieldSchema {
  implicit def encoder[T]: Encoder[FieldSchema[T]] = deriveEncoder

  def apply[T](implicit fs: FieldSchema[T]): FieldSchema[T] = fs
}

object FieldTypeSchemaInstances extends FieldTypeSchemaInstances

trait FieldTypeSchemaInstances {

  implicit val intFTS: FieldSchema[Int] = FieldSchema[Int](Some("int-input"), Some("int-output"), default = Some(0.asJson))

  implicit val shortFTV: FieldSchema[Short] = intFTS.copy()

  implicit val longFTV: FieldSchema[Char] = intFTS.copy()

  implicit val byteFTV: FieldSchema[Byte] = intFTS.copy()

  implicit val charFTV: FieldSchema[Char] = intFTS.copy()

  implicit val floatFTV: FieldSchema[Float] = intFTS.copy()

  implicit val doubleFTV: FieldSchema[Double] = intFTS.copy()

  implicit val bigDecimalFTV: FieldSchema[BigDecimal] = intFTS.copy()

  implicit val bigIntFTV: FieldSchema[BigInt] = intFTS.copy()

  implicit val stringFTS: FieldSchema[String] = FieldSchema[String](Some("string-input"), Some("string-output"), default = Some("".asJson))

  implicit val boolFTS: FieldSchema[Boolean] = FieldSchema[Boolean](Some("bool-input"), Some("bool-output"), default = Some(false.asJson))

  implicit def listFTS[T](implicit elementFTS: FieldSchema[T]): FieldSchema[List[T]] = FieldSchema[List[T]](
    inputType = Some("list-input"),
    outputType = Some("list-output"),
    default = Some(Json.fromValues(List.empty)),
    addition = Some(Map("elementSchema" -> elementFTS).asJson)
    )

  implicit def seqFTS[T](implicit elementFTS: FieldSchema[T]): FieldSchema[Seq[T]] = listFTS[T].copy()

  implicit def setFTS[T](implicit elementFTS: FieldSchema[T]): FieldSchema[Set[T]] = listFTS[T].copy()

  implicit def arrayFTS[T](implicit elementFTS: FieldSchema[T]): FieldSchema[Array[T]] = listFTS[T].copy()

  //This maybe should do something -__-
  implicit def optionFTS[T](implicit elementFTS: FieldSchema[T]): FieldSchema[Option[T]] = elementFTS.copy()

  implicit def enumFTS[T <: Enumeration#Value : TypeTag]: FieldSchema[T] = {
    val enum = getEnum[T]
    val defaultValue = enum.values.head.toString.asJson
    val enumValues = enum.values.map(_.toString)

    FieldSchema(Some("enum-input"), Some("enum-output"), default = Some(defaultValue), addition = Some(Map("values" -> enumValues).asJson))
  }
}
