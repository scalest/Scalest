package scalest.admin

import scala.concurrent.Future

trait CrudRepository[Model, Id] {
  def findAll(): Future[Seq[Model]]

  def create(m: Model): Future[Int]

  def update(m: Model): Future[Model]

  def delete(ids: Seq[Id]): Future[Int]
}
