package scalest.admin.slick

import scalest.admin.ModelRepository
import slick.basic.DatabaseConfig
import scala.concurrent.{ExecutionContext, Future}

object SlickRepository {
  def apply(sm: SlickModel)(implicit dc: DatabaseConfig[_], ec: ExecutionContext): ModelRepository[sm.Model, sm.Id] = {
    new ModelRepository[sm.Model, sm.Id] {
      def findAll(): Future[Seq[sm.Model]] = sm.findAll.run

      def findByIds(ids: Set[sm.Id]): Future[Seq[sm.Model]] = sm.findByIds(ids).run

      def create(m: sm.Model): Future[sm.Id] = sm.create(m).run

      def update(m: sm.Model): Future[Int] = sm.save(m).run

      def delete(ids: Set[sm.Id]): Future[Int] = sm.deleteByIds(ids).run
    }
  }
}