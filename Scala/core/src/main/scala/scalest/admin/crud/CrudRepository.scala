package scalest.admin.crud

import scalest.admin.pagination.{PageRequest, PageResponse}

trait CrudRepository[F[_], Model, Query, Id] {
  def findAll(): F[Seq[Model]]
  def findAll(page: PageRequest): F[PageResponse[Model]]
  def findById(id: Id): F[Model]
  def findOptionById(id: Id): F[Option[Model]]
  def findByIds(ids: Seq[Id]): F[Seq[Model]]
  def findBy(query: Query): F[Seq[Model]]
  def findBy(query: Query, page: PageRequest): F[PageResponse[Model]]

  def countAll(): F[Int]
  def count(query: Query): F[Int]

  def create(model: Model): F[Model]
  def create(models: Seq[Model]): F[Seq[Model]]

  def upsert(models: Seq[Model]): F[Seq[Model]]
  def upsert(model: Model): F[Model]

  def update(ids: Seq[Id], update: Model => Model): F[Seq[Model]]
  def update(id: Id, update: Model => Model): F[Model]

  def deleteAll(): F[Int]
  def delete(query: Query): F[Int]
  def deleteByIds(ids: Seq[Id]): F[Int]
}
