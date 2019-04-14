package scalest.admin

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax._
import io.circe.generic.semiauto._
import scalest.admin.Utils._

import scala.language.experimental.macros
import scala.reflect.runtime.universe.TypeTag

case class FieldTypeSchema[T](inputType: Option[String] = None,
                              outputType: Option[String] = None,
                              default: Option[Json] = None,
                              addition: Option[Json] = None,
                              inputView: Option[String] = None,
                              outputView: Option[String] = None)

object FieldTypeSchema {
  implicit def encoder[T]: Encoder[FieldTypeSchema[T]] = Encoder.instance { fts =>
    import fts._
    Json.fromFields(
      Seq(
        inputType.map("inputType" -> _.asJson).toList,
        outputType.map("outputType" -> _.asJson).toList,
        default.map("default" -> _.asJson).toList,
        addition.map("addition" -> _.asJson).toList,
        inputView.map("inputView" -> _.asJson).toList,
        outputView.map("outputView" -> _.asJson).toList
        ).flatten
      )
  }
}

object FieldTypeSchemaInstances extends FieldTypeSchemaInstances

trait FieldTypeSchemaInstances {

  implicit val intFTS: FieldTypeSchema[Int] = FieldTypeSchema[Int](Some("int-input"), Some("int-output"), Some(0.asJson))

  implicit val shortFTV: FieldTypeSchema[Short] = intFTS.copy()

  implicit val longFTV: FieldTypeSchema[Char] = intFTS.copy()

  implicit val byteFTV: FieldTypeSchema[Byte] = intFTS.copy()

  implicit val charFTV: FieldTypeSchema[Char] = intFTS.copy()

  implicit val floatFTV: FieldTypeSchema[Float] = intFTS.copy()

  implicit val doubleFTV: FieldTypeSchema[Double] = intFTS.copy()

  implicit val bigDecimalFTV: FieldTypeSchema[BigDecimal] = intFTS.copy()

  implicit val bigIntFTV: FieldTypeSchema[BigInt] = intFTS.copy()

  implicit val stringFTS: FieldTypeSchema[String] = FieldTypeSchema[String](Some("string-input"), Some("string-output"), Some("".asJson))

  implicit val boolFTS: FieldTypeSchema[Boolean] = FieldTypeSchema[Boolean](Some("bool-input"), Some("bool-output"), Some(false.asJson))

  implicit def listFTS[T](implicit elementFTS: FieldTypeSchema[T]): FieldTypeSchema[List[T]] = FieldTypeSchema[List[T]](
    inputType = Some("list-input"),
    outputType = Some("list-output"),
    default = Some(Json.fromValues(List.empty)),
    addition = Some(Map("elementSchema" -> elementFTS).asJson)
    )

  implicit def seqFTS[T](implicit elementFTS: FieldTypeSchema[T]): FieldTypeSchema[Seq[T]] = listFTS[T].copy()

  implicit def setFTS[T](implicit elementFTS: FieldTypeSchema[T]): FieldTypeSchema[Set[T]] = listFTS[T].copy()

  implicit def arrayFTS[T](implicit elementFTS: FieldTypeSchema[T]): FieldTypeSchema[Array[T]] = listFTS[T].copy()

  //This maybe should do something -__-
  implicit def optionFTS[T](implicit elementFTS: FieldTypeSchema[T]): FieldTypeSchema[Option[T]] = elementFTS.copy()

  implicit def enumFTS[T <: Enumeration#Value : TypeTag]: FieldTypeSchema[T] = {
    val enum = getEnum[T]
    val defaultValue = enum.values.head.toString.asJson
    val enumValues = enum.values.map(_.toString)

    FieldTypeSchema(Some("enum-input"), Some("enum-output"), Some(defaultValue), Some(Map("values" -> enumValues).asJson))
  }
}
