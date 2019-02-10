package scalest.admin.users

import cats.ApplicativeError
import cats.implicits.toFunctorOps
import scalest.admin.{ModelAdmin, ModelExtension}
import scalest.auth.{AuthRequest, AuthResponse, User, UserTokenCodec}
import scalest.error
import scalest.tapir._
import sttp.tapir.server.ServerEndpoint

final class UsersModelAdmin[F[_]: ApplicativeError[*[_], Throwable]](
  service: UsersModelService[F],
  extensions: List[ModelExtension[F, User, String]],
)(
  implicit UTC: UserTokenCodec,
) extends ModelAdmin[F, User, String](service, extensions) {
  val loginEndpoint: ServerEndpoint[AuthRequest, error.CommonError, AuthResponse, Nothing, F] =
    commonEndpoint("login").post
      .in("admin" / "login")
      .in(jsonBody[AuthRequest])
      .out(jsonBody[AuthResponse])
      .tapir { request =>
        service
          .login(request.username, request.password)
          .map(UTC.encodeUser)
          .map(AuthResponse.apply)
      }

  override def endpoints: List[ServerEndpoint[_, _, _, Nothing, F]] = List(loginEndpoint) ++ super.endpoints
}
