package scalest.admin.schema

import java.time.LocalDateTime

import io.circe.Json._
import io.circe.syntax._
import io.circe.{Encoder, Json}
import io.scalaland.chimney.dsl.TransformerOps
import scalest.admin.dto.FieldSchemaDto
import scalest.admin.schema.FieldSchema.instance
import scalest.meta.reflectOuterEnum

import scala.language.experimental.macros
import scala.language.implicitConversions
import scala.reflect.runtime.universe._

case class FieldSchema[T](
  inputType: Option[String] = None,
  outputType: Option[String] = None,
  default: Option[Json] = None,
  addition: Option[Json] = None,
  isOptional: Boolean = false,
) {
  def toDto: FieldSchemaDto = this.transformInto[FieldSchemaDto]
  def withDefault[E: Encoder](default: E): FieldSchema[T] = copy(default = Some(default.asJson))
  def withAddition[E: Encoder](addition: E): FieldSchema[T] = copy(addition = Some(addition.asJson))
}

object FieldSchema {
  def instance[T](
    name: String,
    default: Option[Json] = None,
    addition: Option[Json] = None,
    isOptional: Boolean = false,
  ): FieldSchema[T] =
    FieldSchema[T](Some(s"$name-input"), Some(s"$name-output"), default, addition, isOptional)
}

object FieldTypeSchemaInstances extends FieldTypeSchemaInstances

trait FieldTypeSchemaInstances {
  implicit val intFTS: FieldSchema[Int] = instance[Int]("int").withDefault(0)
  implicit val shortFTV: FieldSchema[Short] = intFTS.copy()
  implicit val longFTV: FieldSchema[Char] = intFTS.copy()
  implicit val byteFTV: FieldSchema[Byte] = intFTS.copy()
  implicit val charFTV: FieldSchema[Char] = intFTS.copy()
  implicit val bigIntFTV: FieldSchema[BigInt] = intFTS.copy()
  implicit val doubleFTV: FieldSchema[Double] = instance[Double]("double").withDefault(0.0)
  implicit val floatFTV: FieldSchema[Float] = doubleFTV.copy()
  implicit val bigDecimalFTV: FieldSchema[BigDecimal] = doubleFTV.copy()
  implicit val stringFTS: FieldSchema[String] = instance[String]("string").withDefault("")
  implicit val jsonFTS: FieldSchema[Json] = instance[Json]("json").withDefault(Json.Null)
  implicit val boolFTS: FieldSchema[Boolean] = instance[Boolean]("bool").withDefault(false)
  implicit val localDateTimeFTS: FieldSchema[LocalDateTime] =
    instance[LocalDateTime]("date-time").withDefault(LocalDateTime.now())
  implicit def listFTS[T](implicit elementFTS: FieldSchema[T]): FieldSchema[List[T]] =
    instance[List[T]]("list", default = Some(arr())).withAddition(Map("elementSchema" -> elementFTS.toDto))
  implicit def seqFTS[T](implicit elementFTS: FieldSchema[T]): FieldSchema[Seq[T]] = listFTS[T].copy()
  implicit def setFTS[T](implicit elementFTS: FieldSchema[T]): FieldSchema[Set[T]] = listFTS[T].copy()
  implicit def arrayFTS[T](implicit elementFTS: FieldSchema[T]): FieldSchema[Array[T]] = listFTS[T].copy()
  implicit def optionFTS[T](implicit elementFTS: FieldSchema[T]): FieldSchema[Option[T]] =
    elementFTS.copy(isOptional = true)
  implicit def enumFTS[E <: Enumeration#Value: WeakTypeTag]: FieldSchema[E] = {
    val values = reflectOuterEnum[E].values.toList
    instance[E]("enum")
      .withDefault(values.head.toString)
      .withAddition(Map("values" -> values.map(_.toString)))
  }
}
