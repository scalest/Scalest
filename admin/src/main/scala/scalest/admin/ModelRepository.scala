package scalest.admin

import scala.concurrent.Future

trait ModelRepository[Model, Id] {
  def findAll(): Future[Seq[Model]]

  def findByIds(ids: Set[Id]): Future[Seq[Model]]

  def create(m: Model): Future[Id]

  def update(m: Model): Future[Int]

  def delete(ids: Set[Id]): Future[Int]
}
