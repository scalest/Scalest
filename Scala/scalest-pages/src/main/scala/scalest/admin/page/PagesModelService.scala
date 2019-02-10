package scalest.admin.page

import cats.effect.Sync
import scalest.service.{GenId, ModelService}

trait PagesModelService[F[_]] extends ModelService[F, Page, String] {
  def find(path: String): F[Option[Page]]
}

object PagesModelService {
  implicit val genUserId: GenId[String] = GenId.genUUID
  class Dummy[F[_]: Sync] extends ModelService.Dummy[F, Page, String](_.id) with PagesModelService[F] {
    def find(path: String): F[Option[Page]] = find(_.path == path)
  }
}
