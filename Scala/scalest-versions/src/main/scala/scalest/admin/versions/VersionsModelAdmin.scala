package scalest.admin.versions

import cats.ApplicativeError
import io.circe.Encoder
import scalest.admin.ModelAdmin
import scalest.admin.schema.ModelSchema
import scalest.auth.UserTokenCodec

final class VersionsModelAdmin[F[_]: ApplicativeError[*[_], Throwable]](repo: VersionsModelService[F])(
  implicit UTC: UserTokenCodec,
) extends ModelAdmin[F, ModelVersion, String](repo, List.empty) {
  def extension[M: ModelSchema: Encoder, I: Encoder]: VersionsExtension[F, M, I] = VersionsExtension[F, M, I](repo)
}
