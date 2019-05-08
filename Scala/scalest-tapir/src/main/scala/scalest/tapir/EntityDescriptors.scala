package scalest.tapir

import io.circe.Encoder
import io.circe.Decoder
import scalest.admin.pagination.PageResponse
import sttp.tapir.Codec.JsonCodec
import sttp.tapir.Schema
import sttp.tapir.Validator

import scala.language.implicitConversions

case class EntityDescriptors[T](encoder: Encoder[T], decoder: Decoder[T], schema: Schema[T], validator: Validator[T]) {
  implicit val (e, d, s, v) = (encoder, decoder, schema, validator)
  def jsonCodec: JsonCodec[T] = implicitly[JsonCodec[T]]
  def seqJsonCodec: JsonCodec[Seq[T]] = implicitly[JsonCodec[Seq[T]]]
  def pageResponseJsonCodec: JsonCodec[PageResponse[T]] = implicitly[JsonCodec[PageResponse[T]]]
}

object EntityDescriptors {
  implicit def create[T](
    implicit
    encoder: Encoder[T],
    decoder: Decoder[T],
    schema: Schema[T],
    validator: Validator[T]
  ): EntityDescriptors[T] = EntityDescriptors[T](encoder, decoder, schema, validator)
}
