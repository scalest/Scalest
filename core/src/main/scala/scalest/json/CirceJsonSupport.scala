package scalest.json

import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import io.circe.Decoder.Result
import io.circe.generic.decoding.DerivedDecoder
import io.circe.generic.encoding.DerivedObjectEncoder
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, HCursor, Json}
import shapeless.Lazy


case class JsonConverter[T](encoder: Encoder[T], decoder: Decoder[T]) extends Encoder[T] with Decoder[T] {

  override def apply(a: T): Json = encoder.apply(a)

  override def apply(c: HCursor): Result[T] = decoder.apply(c)

}

trait CirceJsonSupport
  extends ErrorAccumulatingCirceSupport {

  def circeObject[T](implicit decode: Lazy[DerivedDecoder[T]],
                     encode: Lazy[DerivedObjectEncoder[T]]): JsonConverter[T] = JsonConverter(deriveEncoder[T], deriveDecoder[T])

  def circeEnum[E <: Enumeration](enum: E): JsonConverter[E#Value] = JsonConverter(Encoder.enumEncoder(enum), Decoder.enumDecoder(enum))

}

object CirceJsonSupport
  extends CirceJsonSupport