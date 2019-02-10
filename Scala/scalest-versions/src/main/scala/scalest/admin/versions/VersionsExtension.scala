package scalest.admin.versions

import cats.ApplicativeError
import cats.implicits.toFunctorOps
import io.circe.Encoder
import io.circe.syntax._
import scalest.admin.ModelExtension
import scalest.admin.schema.ModelSchema
import scalest.auth.User
import scalest.service.{GenId, ModelService}

case class VersionsExtension[
  F[_]: ApplicativeError[*[_], Throwable],
  M: ModelSchema: Encoder,
  I: Encoder,
](repo: ModelService[F, ModelVersion, String])
    extends ModelExtension[F, M, I] {
  private val modelName: String = ModelSchema[M].name
  override def afterUpdate(user: User, model: M): F[Unit] =
    repo.create(ModelVersion(GenId.genUUID.gen, modelName, ModelActions.Update, model.asJson, user.id)).void

  override def afterDelete(user: User, ids: Seq[I]): F[Unit] =
    repo.create(ModelVersion(GenId.genUUID.gen, modelName, ModelActions.Delete, ids.asJson, user.id)).void

  override def afterCreate(user: User, model: M): F[Unit] =
    repo.create(ModelVersion(GenId.genUUID.gen, modelName, ModelActions.Create, model.asJson, user.id)).void

}
