package scalest.admin.users

import cats.syntax.applicativeError.catsSyntaxApplicativeErrorId
import cats.{Applicative, ApplicativeError}
import scalest.admin.ModelExtension
import scalest.admin.schema.ModelSchema
import scalest.auth.Permission._
import scalest.auth.User
import scalest.exception.ApplicationException

case class UsersExtension[F[_]: ApplicativeError[*[_], Throwable], M: ModelSchema, I](repo: UsersModelService[F])
    extends ModelExtension[F, M, I] {
  private val name: String = ModelSchema[M].name
  def checkPermission(check: Boolean, error: String): F[Unit] =
    Applicative[F].whenA(check)(ApplicationException(error).raiseError[F, Unit])

  override def beforeUpdate(user: User, model: M): F[Unit] =
    checkPermission(user.hasPermission(UpdatePermission(name)), s"You don't have update permission for $name")

  override def beforeDelete(user: User, ids: Seq[I]): F[Unit] =
    checkPermission(user.hasPermission(DeletePermission(name)), s"You don't have delete permission for $name")

  override def beforeCreate(user: User, model: M): F[Unit] =
    checkPermission(user.hasPermission(CreatePermission(name)), s"You don't have create permission for $name")
}
