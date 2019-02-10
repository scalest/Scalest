package scalest.auth

import io.circe.parser.parse
import io.circe.syntax._
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}

trait UserTokenCodec {
  def decodeUser(token: String): Option[User]
  def encodeUser(user: User): String
}

object UserTokenCodec {
  case class JwtSecretCodec(secret: String) extends UserTokenCodec {
    def encodeUser(user: User): String = {
      val claim = JwtClaim(content = user.asJson.noSpaces)
      JwtCirce.encode(claim, secret, JwtAlgorithm.HS256)
    }

    def decodeUser(token: String): Option[User] =
      JwtCirce
        .decode(token, secret, Seq(JwtAlgorithm.HS256))
        .map(_.content)
        .toOption
        .flatMap(parse(_).toOption)
        .flatMap(_.as[User].toOption)
  }
}
