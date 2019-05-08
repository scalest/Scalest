package scalest.tapir

import scalest.error.MalformedFieldErrorMessage
import sttp.tapir.Codec.PlainCodec
import sttp.tapir.DecodeResult.{Error, Value}
import sttp.tapir.{Codec, CodecForMany, DecodeResult, EndpointInput}

import scala.util.Try

trait TapirJwtAuth {
  private val BearerAuthType = "Bearer"
  private def bearerAuth[T](codec: PlainCodec[T]): EndpointInput.Auth.Http[T] = {
    EndpointInput.Auth.Http(BearerAuthType, header[T]("Authorization")(CodecForMany.fromCodec(codec)))
  }
  private val bearerCodec: PlainCodec[String] = {
    val authTypeWithSpace = BearerAuthType + " "
    val prefixLength = authTypeWithSpace.length

    def removeAuthType(v: String): DecodeResult[String] =
      if (v.startsWith(BearerAuthType)) DecodeResult.Value(v.substring(prefixLength))
      else DecodeResult.Error(v, new IllegalArgumentException(s"The given value doesn't start with $BearerAuthType"))

    Codec.stringPlainCodecUtf8.mapDecode(removeAuthType)(v => s"$BearerAuthType $v")
  }

  def jwtClaimCodec[T](decode: String => Try[T], encode: T => String): PlainCodec[T] = {
    bearerCodec.mapDecode[T](decode(_).fold(Error(MalformedFieldErrorMessage, _), Value(_)))(encode)
  }

  def jwtAuth[T](decode: String => Try[T], encode: T => String): EndpointInput.Auth.Http[T] = {
    bearerAuth(jwtClaimCodec(decode, encode))
  }
}
