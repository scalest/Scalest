package scalest.admin

import scala.concurrent.Future

trait CrudRepository[Model] {
  def findAll(): Future[Seq[Model]]

  def create(m: Model): Future[Int]

  def upsert(m: Model): Future[Model]

  def delete(id: Int): Future[Int]

  def deleteAll(ids: Seq[Int]): Future[Int]
}
