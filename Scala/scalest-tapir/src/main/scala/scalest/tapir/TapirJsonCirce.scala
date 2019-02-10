package scalest.tapir

import java.nio.charset.StandardCharsets

import io.circe.parser.parse
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json, Printer}
import sttp.tapir.Codec.JsonCodec
import sttp.tapir.DecodeResult.{Error, Value}
import sttp.tapir.SchemaType.{SObjectInfo, SProduct}
import sttp.tapir.{CodecFormat, CodecMeta, DecodeResult, Schema, StringValueType, Validator}

trait TapirJsonCirce {
  implicit def encoderDecoderCodec[T: Encoder: Decoder: Schema: Validator]: JsonCodec[T] = new JsonCodec[T] {
    override def encode(t: T): String = jsonPrinter.print(t.asJson)
    override def rawDecode(s: String): DecodeResult[T] =
      parse(s).fold(
        _ => Error(s, DecodeException()),
        json =>
          implicitly[Decoder[T]]
            .decodeAccumulating(json.hcursor)
            .fold(
              failures => Error(s, DecodeException(failures.toList)),
              Value(_),
            ),
      )
    override def meta: CodecMeta[T, CodecFormat.Json, String] =
      CodecMeta(
        implicitly[Schema[T]],
        CodecFormat.Json(),
        StringValueType(StandardCharsets.UTF_8),
        implicitly[Validator[T]],
      )
  }
  def jsonPrinter: Printer = Printer.noSpaces
  implicit val schemaForCirceJson: Schema[Json] = Schema(SProduct(SObjectInfo("io.circe.Json"), List.empty))
}
