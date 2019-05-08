package scalest.admin.slick

import scalest.admin.FutureToEffect
import scalest.admin.crud.CrudRepository
import scalest.admin.pagination.{PageRequest, PageResponse}
import scalest.admin.slick.meta.ModelQueryMacros
import scalest.exception.EntityNotFoundException
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext
import scala.language.experimental.macros

trait SlickCrudRepositoryModule { self: SlickSearchModule with SlickActionModule =>
  type Profile <: JdbcProfile
  val profile: Profile

  abstract class SlickCrudRepository[F[_]: FutureToEffect, M, Q <: ModelQuery, T <: Profile#Table[M]](
    implicit
    val databaseConfig: DatabaseConfig[Profile],
    val executionContext: ExecutionContext
  ) extends CrudRepository[F, M, Q, Q#Id] {
    import profile.api._

    def queryFactory: ModelQueryFactory[Q]

    def tableQuery: TableQuery[T]

    def NotFoundError: Throwable = EntityNotFoundException(s"error.${tableQuery.baseTableRow.tableName}.not-found")

    def searchQuery(queryP: Q, excludeP: List[String]): Query[T, M, Seq] = macro ModelQueryMacros.impl[Q, M, T]

    def findByQuery(query: Q): Query[T, M, Seq]

    def findOneByQueryAction(query: Q): SlickAction[M] =
      findByQuery(query).result.headOption.flatMap {
        case Some(fs) => DBIO.successful(fs)
        case None     => DBIO.failed(NotFoundError)
      }.slickAction

    def findAllAction(): SlickAction[Seq[M]] = tableQuery.result.slickAction

    def findAllAction(page: PageRequest): SlickAction[PageResponse[M]] =
      (for {
        tasks <- tableQuery.drop(page.offset).take(page.size).result
        count <- countAllAction().dbio
      } yield PageResponse.response(tasks, count, page)).transactionally.slickAction

    def findByIdAction(id: Q#Id): SlickAction[M] = findOneByQueryAction(queryFactory.fromId(id))

    def findOptionByQueryAction(query: Q): SlickAction[Option[M]] = findByQuery(query).result.headOption.slickAction

    def findOptionByIdAction(id: Q#Id): SlickAction[Option[M]] = findOptionByQueryAction(queryFactory.fromId(id))

    def findByQueryAction(query: Q): SlickAction[Seq[M]] = findByQuery(query).result.slickAction

    def findByIdsAction(ids: Seq[Q#Id]): SlickAction[Seq[M]] = findByQueryAction(queryFactory.fromIds(ids))

    def findByQueryAction(query: Q, page: PageRequest): SlickAction[PageResponse[M]] =
      (for {
        tasks <- findByQuery(query).drop(page.offset).take(page.size).result
        count <- countAction(query).dbio
      } yield PageResponse.response(tasks, count, page)).transactionally.slickAction

    def countAllAction(): SlickAction[Int] = tableQuery.size.result.slickAction
    def countAction(query: Q): SlickAction[Int] = findByQuery(query).size.result.slickAction

    def upsertAction(entities: Seq[M]): SlickAction[Seq[M]] =
      DBIO.sequence(entities.map(tableQuery.insertOrUpdate)).slickAction.map(_ => entities)
    def upsertAction(entity: M): SlickAction[M] = tableQuery.insertOrUpdate(entity).slickAction.map(_ => entity)

    def updateAction(ids: Seq[Q#Id], update: M => M): SlickAction[Seq[M]] =
      findByIdsAction(ids).dbio
        .flatMap(e => upsertAction(e.map(update)).dbio)
        .transactionally
        .slickAction

    def updateAction(id: Q#Id, update: M => M): SlickAction[M] =
      findByIdAction(id).dbio
        .flatMap(e => upsertAction(update(e)).dbio)
        .transactionally
        .slickAction

    def createAction(entities: Seq[M]): SlickAction[Seq[M]] = tableQuery.returning(tableQuery).++=(entities).slickAction
    def createAction(entity: M): SlickAction[M] = (tableQuery += entity).slickAction.map(_ => entity)

    def deleteAction(query: Q): SlickAction[Int] = findByQuery(query).delete.slickAction
    def deleteAllAction(): SlickAction[Int] = tableQuery.delete.slickAction
    def deleteByIdsAction(ids: Seq[Q#Id]): SlickAction[Int] = deleteAction(queryFactory.fromIds(ids))

    override def findAll(): F[Seq[M]] = findAllAction().effect[F]
    override def findAll(page: PageRequest): F[PageResponse[M]] = findAllAction(page).effect[F]
    override def findById(id: Q#Id): F[M] = findByIdAction(id).effect[F]
    override def findOptionById(id: Q#Id): F[Option[M]] = findOptionByIdAction(id).effect[F]
    override def findByIds(ids: Seq[Q#Id]): F[Seq[M]] = findByIdsAction(ids).effect[F]
    override def findBy(query: Q): F[Seq[M]] = findByQueryAction(query).effect[F]
    override def findBy(query: Q, page: PageRequest): F[PageResponse[M]] = findByQueryAction(query, page).effect[F]
    override def countAll(): F[Int] = countAllAction().effect[F]
    override def count(query: Q): F[Int] = countAction(query).effect[F]
    override def create(model: M): F[M] = createAction(model).effect[F]
    override def create(models: Seq[M]): F[Seq[M]] = createAction(models).effect[F]
    override def upsert(models: Seq[M]): F[Seq[M]] = upsertAction(models).effect[F]
    override def upsert(model: M): F[M] = upsertAction(model).effect[F]
    override def update(ids: Seq[Q#Id], update: M => M): F[Seq[M]] = updateAction(ids, update).effect[F]
    override def update(id: Q#Id, update: M => M): F[M] = updateAction(id, update).effect[F]
    override def deleteAll(): F[Int] = deleteAllAction().effect[F]
    override def delete(query: Q): F[Int] = deleteAction(query).effect[F]
    override def deleteByIds(ids: Seq[Q#Id]): F[Int] = deleteByIdsAction(ids).effect[F]
  }

}
