package scalest.auth

import cats.ApplicativeError
import cats.implicits._
import scalest.exception.ApplicationException

trait AuthService[F[_]] {
  def login(login: AuthRequest): F[AuthResponse]
}

object AuthService {
  def default[F[_]](implicit AE: ApplicativeError[F, Throwable]): AuthService[F] = new AuthService[F] {
    override def login(credentials: AuthRequest): F[AuthResponse] = credentials match {
      case AuthRequest(AdminUsername, AdminPassword) => AuthResponse(Token).pure[F]
      case _                                         => ApplicationException(CredentialsIncorrect).raiseError[F, AuthResponse]
    }
  }
}