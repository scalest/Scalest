package scalest.admin.versions

import cats.effect.Sync
import scalest.service.{GenId, ModelService}

trait VersionsModelService[F[_]] extends ModelService[F, ModelVersion, String] {
  final override val genId: GenId[String] = VersionsModelService.genModelVersionId
}
object VersionsModelService {
  implicit val genModelVersionId: GenId[String] = GenId.genUUID
  class Dummy[F[_]: Sync] extends ModelService.Dummy[F, ModelVersion, String](_.id) with VersionsModelService[F]
}
