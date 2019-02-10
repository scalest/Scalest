package scalest.tapir

import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import io.circe.{Decoder, Encoder}
import scalest.error.MalformedFieldErrorMessage
import scalest.meta.reflectOuterEnum
import sttp.model.MediaType
import sttp.tapir.Codec.{JsonCodec, PlainCodec}
import sttp.tapir.DecodeResult.{Error, Value}
import sttp.tapir.{
  Codec,
  CodecForOptional,
  CodecFormat,
  CodecMeta,
  DecodeResult,
  EndpointIO,
  Schema,
  SchemaType,
  StringValueType,
  Validator,
}

import scala.reflect.runtime.universe._
import scala.util.Try

trait TapirCommonInstances { self: TapirJsonCirce =>
  implicit val localDateTime: PlainCodec[LocalDateTime] = {
    Codec.stringPlainCodecUtf8.map(LocalDateTime.parse)(_.toString).schema(Schema.schemaForLocalDateTime)
  }
  implicit val chronoUnitSchema: Schema[ChronoUnit] = Schema(SchemaType.SString)
  implicit val chronoUnitValidator: Validator[ChronoUnit] = Validator.enum(ChronoUnit.values.toList).encode(_.toString)

  implicit def enumSchemaFor[E <: Enumeration#Value: WeakTypeTag]: Schema[E] = {
    val enum = reflectOuterEnum[E]
    Schema(SchemaType.SString).description(s"Enum: ${enum.values.mkString(", ")}")
  }

  implicit def enumValidator[E <: Enumeration#Value: WeakTypeTag]: Validator[E] = {
    val enum = reflectOuterEnum[E]
    Validator.enum[E](enum.values.toList.asInstanceOf[List[E]]).encode(_.toString)
  }

  implicit def plainEnumCodec[E <: Enumeration#Value: WeakTypeTag]: PlainCodec[E] = {
    val enum = reflectOuterEnum[E]
    Codec.stringPlainCodecUtf8
      .mapDecode(s => Try(enum.withName(s).asInstanceOf[E]).fold(Error(MalformedFieldErrorMessage, _), Value(_)))(
        _.toString,
      )
      .validate(enumValidator[E])
  }

  implicit def jsonEnumCodec[E <: Enumeration#Value: WeakTypeTag: Encoder: Decoder]: JsonCodec[E] =
    encoderDecoderCodec[E](implicitly, implicitly, enumSchemaFor[E], enumValidator[E])

  abstract class CustomCodecFormat(mt: MediaType) extends CodecFormat {
    override val mediaType: MediaType = mt
  }

  case class TextCsv() extends CustomCodecFormat(MediaType.TextCsv)

  def csvCodec[T: Schema](parse: String => T): Codec[T, TextCsv, String] = customCodec(TextCsv(), parse)
  def customCodec[T: Schema, C <: CodecFormat](format: C, parse: String => T): Codec[T, C, String] =
    new Codec[T, C, String] {
      override def encode(t: T): String = t.toString
      override def rawDecode(s: String): DecodeResult[T] = Try(parse(s)).fold(Error(s, _), Value(_))
      override val meta: CodecMeta[T, C, String] =
        CodecMeta(implicitly, format, StringValueType(StandardCharsets.UTF_8))
    }
  def csvBody[T](implicit codec: CodecForOptional[T, TextCsv, _]): EndpointIO.Body[T, TextCsv, _] =
    EndpointIO.Body(codec, EndpointIO.Info.empty)
  implicit val stringPlainCodecUtf8: Codec[String, TextCsv, String] = csvCodec(identity)
}
