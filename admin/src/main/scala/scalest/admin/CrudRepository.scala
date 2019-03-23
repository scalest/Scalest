package scalest.admin

import scala.concurrent.Future

trait CrudRepository[Model, Id] {
  def findAll(): Future[Seq[Model]]

  def create(m: Model): Future[Id]

  def update(m: Model): Future[Model]

  def delete(ids: Set[Id]): Future[Int]
}
