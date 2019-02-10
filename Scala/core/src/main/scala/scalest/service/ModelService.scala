package scalest.service

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.implicits.toFunctorOps
import scalest.admin.action.{Action, SearchAction}
import scalest.admin.pagination.PageResponse.response
import scalest.admin.pagination.{PageRequest, PageResponse}

trait ModelService[F[_], Model, Id] {
  val genId: GenId[Id]
  val actions: List[Action[F, _]] = List.empty
  val searchActions: List[SearchAction[F, Model, _]] = List.empty
  def create(model: Model): F[Model]
  def upsert(model: Model): F[Model]
  def delete(ids: Seq[Id]): F[Unit]
  def findAll(page: PageRequest): F[PageResponse[Model]]
}

object ModelService {

  class Dummy[F[_]: Sync, M, I: GenId](id: M => I) extends ModelService[F, M, I] {
    val genId: GenId[I] = GenId[I]
    val store: Ref[F, Map[I, M]] = Ref.unsafe[F, Map[I, M]](Map.empty)
    def create(model: M): F[M] = store.update(_ + (id(model) -> model)).as(model)
    def upsert(model: M): F[M] = store.update(_.updated(id(model), model)).as(model)
    def delete(ids: Seq[I]): F[Unit] = store.update(_ -- ids).void
    def findAll(page: PageRequest): F[PageResponse[M]] =
      store.get
        .map(_.values.slice(page.offset, page.offset + page.size))
        .map(m => response(m.toSeq, m.size, page))
    def filter(query: M => Boolean): F[List[M]] = store.get.map(_.values.filter(query).toList)
    def find(query: M => Boolean): F[Option[M]] = store.get.map(_.values.find(query))
  }

}
