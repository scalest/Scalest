package scalest.admin

import cats.ApplicativeError
import scalest.auth.{AuthRequest, AuthResponse, AuthService}
import scalest.error
import scalest.tapir._
import sttp.tapir.server.ServerEndpoint

case class AuthModule[F[_]](authService: AuthService[F])(implicit C: ApplicativeError[F, Throwable]) extends TapirModule[F] {

  val loginEndpoint: ServerEndpoint[AuthRequest, error.CommonError, AuthResponse, Nothing, F] = {
    commonEndpoint("login").post
      .in("admin" / "login")
      .in(jsonBody[AuthRequest])
      .out(jsonBody[AuthResponse])
      .tapir(authService.login)
  }

  override val routes: List[ServerEndpoint[_, _, _, Nothing, F]] = List(loginEndpoint)
}
