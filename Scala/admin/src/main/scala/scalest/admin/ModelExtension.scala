package scalest.admin

import cats.Applicative
import cats.implicits.catsSyntaxApply
import scalest.auth.User

abstract class ModelExtension[F[_]: Applicative, M, I] { ext =>
  def afterUpdate(user: User, model: M): F[Unit] = Applicative[F].unit
  def afterDelete(user: User, ids: Seq[I]): F[Unit] = Applicative[F].unit
  def afterCreate(user: User, model: M): F[Unit] = Applicative[F].unit
  def beforeUpdate(user: User, model: M): F[Unit] = Applicative[F].unit
  def beforeDelete(user: User, ids: Seq[I]): F[Unit] = Applicative[F].unit
  def beforeCreate(user: User, model: M): F[Unit] = Applicative[F].unit

  def compose(other: ModelExtension[F, M, I]): ModelExtension[F, M, I] = new ModelExtension[F, M, I] {
    override def afterUpdate(user: User, model: M): F[Unit] =
      ext.afterUpdate(user, model) *> other.afterUpdate(user, model)
    override def afterDelete(user: User, ids: Seq[I]): F[Unit] =
      ext.afterDelete(user, ids) *> other.afterDelete(user, ids)
    override def afterCreate(user: User, model: M): F[Unit] =
      ext.afterCreate(user, model) *> other.afterCreate(user, model)
    override def beforeUpdate(user: User, model: M): F[Unit] =
      ext.beforeUpdate(user, model) *> other.beforeUpdate(user, model)
    override def beforeDelete(user: User, ids: Seq[I]): F[Unit] =
      ext.beforeDelete(user, ids) *> other.beforeDelete(user, ids)
    override def beforeCreate(user: User, model: M): F[Unit] =
      ext.beforeCreate(user, model) *> other.beforeCreate(user, model)
  }
}

object ModelExtension {
  def empty[F[_]: Applicative, M, I]: ModelExtension[F, M, I] = new ModelExtension[F, M, I] {}
}
