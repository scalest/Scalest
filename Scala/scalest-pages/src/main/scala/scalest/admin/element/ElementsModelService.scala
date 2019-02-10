package scalest.admin.element

import cats.effect.Sync
import scalest.admin.element.ElementModel.Element
import scalest.service.{GenId, ModelService}

trait ElementsModelService[F[_]] extends ModelService[F, Element, String] {
  def findByIds(ids: Set[String]): F[List[Element]]
}

object ElementsModelService {
  implicit val genUserId: GenId[String] = GenId.genUUID
  class Dummy[F[_]: Sync] extends ModelService.Dummy[F, Element, String](_.id) with ElementsModelService[F] {
    override def findByIds(ids: Set[String]): F[List[Element]] = filter(e => ids(e.id))
  }
}
