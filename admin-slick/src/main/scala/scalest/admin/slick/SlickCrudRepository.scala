package scalest.admin.slick

import scalest.admin.CrudRepository
import slick.basic.DatabaseConfig

import scala.concurrent.{ExecutionContext, Future}

object SlickCrudRepository {
  def apply(sm: SlickModel)(implicit dbConfig: DatabaseConfig[_], ec: ExecutionContext): CrudRepository[sm.Model, sm.Id] = {
    new CrudRepository[sm.Model, sm.Id] {
      override def findAll(): Future[Seq[sm.Model]] = dbConfig.db.run(sm.findAll)

      override def create(m: sm.Model): Future[sm.Id] = dbConfig.db.run(sm.create(m))

      override def update(m: sm.Model): Future[sm.Model] = dbConfig.db.run(sm.update(m))

      override def delete(ids: Set[sm.Id]): Future[Int] = dbConfig.db.run(sm.deleteByIds(ids))
    }
  }
}