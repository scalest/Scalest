package scalest.admin.slick

import scalest.admin.FutureToEffect
import scalest.admin.pagination.{PageRequest, PageResponse}
import scalest.exception.EntityNotFoundException
import scalest.service.ModelService
import slick.basic.DatabaseConfig
import slick.jdbc.{JdbcProfile, JdbcType}

import scala.concurrent.ExecutionContext
import scala.language.experimental.macros

trait SlickCrudRepositoryModule extends SlickModuleBase { self: SlickSearchModule with SlickActionModule =>

  abstract class SlickModelService[F[_]: FutureToEffect, M, I, T <: Profile#Table[M]](
    implicit
    val databaseConfig: DatabaseConfig[Profile],
    val executionContext: ExecutionContext,
  ) extends ModelService[F, M, I] {
    import profile.api._
    def tableQuery: TableQuery[T]

    def NotFoundError: Throwable = EntityNotFoundException(s"error.${tableQuery.baseTableRow.tableName}.not-found")

//    def genParamQuery(excludeP: List[String]): Query[T, M, Seq] = macro ModelQueryMacros.impl[Q, M, T]

    case class IdParam(extract: T => Rep[I])(implicit val jdbcType: JdbcType[I])
    def idParam: IdParam
    def idQuery(id: I): Query[T, M, Seq] = {
      implicit val jdbcType: JdbcType[I] = idParam.jdbcType
      tableQuery.filter(t => idParam.extract(t) === id)
    }
    def idsQuery(ids: Seq[I]): Query[T, M, Seq] = {
      implicit val jdbcType: JdbcType[I] = idParam.jdbcType
      tableQuery.filter(t => idParam.extract(t).inSet(ids))
    }

    def findAllAction(): SlickAction[Seq[M]] = tableQuery.result.slickAction

    def findAllAction(page: PageRequest): SlickAction[PageResponse[M]] =
      (for {
        tasks <- tableQuery.drop(page.offset).take(page.size).result
        count <- countAllAction().dbio
      } yield PageResponse.response(tasks, count, page)).transactionally.slickAction

    def findByIdAction(id: I): SlickAction[M] =
      idQuery(id).result.headOption.flatMap {
        case Some(fs) => DBIO.successful(fs)
        case None     => DBIO.failed(NotFoundError)
      }.slickAction

    def findOptionByIdAction(id: I): SlickAction[Option[M]] = idQuery(id).result.headOption.slickAction

    def findByIdsAction(ids: Seq[I]): SlickAction[Seq[M]] = idsQuery(ids).result.slickAction

    def countAllAction(): SlickAction[Int] = tableQuery.size.result.slickAction

    def upsertAction(entities: Seq[M]): SlickAction[Seq[M]] =
      DBIO.sequence(entities.map(tableQuery.insertOrUpdate)).map(_ => entities).slickAction

    def upsertAction(entity: M): SlickAction[M] = tableQuery.insertOrUpdate(entity).map(_ => entity).slickAction

    def updateAction(ids: Seq[I], update: M => M): SlickAction[Seq[M]] =
      findByIdsAction(ids).dbio
        .flatMap(e => upsertAction(e.map(update)).dbio)
        .transactionally
        .slickAction

    def updateAction(id: I, update: M => M): SlickAction[M] =
      findByIdAction(id).dbio
        .flatMap(e => upsertAction(update(e)).dbio)
        .transactionally
        .slickAction

    def createAction(entities: Seq[M]): SlickAction[Seq[M]] = tableQuery.returning(tableQuery).++=(entities).slickAction
    def createAction(entity: M): SlickAction[M] = (tableQuery += entity).map(_ => entity).slickAction

    def deleteAllAction(): SlickAction[Int] = tableQuery.delete.slickAction
    def deleteByIdsAction(ids: Seq[I]): SlickAction[Unit] = idsQuery(ids).delete.slickAction.unit

    //
    override def upsert(model: M): F[M] = upsertAction(model).effect[F]
    override def create(model: M): F[M] = createAction(model).effect[F]
    override def delete(ids: Seq[I]): F[Unit] = deleteByIdsAction(ids).effect[F]
    override def findAll(page: PageRequest): F[PageResponse[M]] = findAllAction(page).effect[F]
    //
    def findAll(): F[Seq[M]] = findAllAction().effect[F]
    def findById(id: I): F[M] = findByIdAction(id).effect[F]
    def findOptionById(id: I): F[Option[M]] = findOptionByIdAction(id).effect[F]
    def findByIds(ids: Seq[I]): F[Seq[M]] = findByIdsAction(ids).effect[F]
    def countAll(): F[Int] = countAllAction().effect[F]
    def create(models: Seq[M]): F[Seq[M]] = createAction(models).effect[F]
    def upsert(models: Seq[M]): F[Seq[M]] = upsertAction(models).effect[F]
    def update(ids: Seq[I], update: M => M): F[Seq[M]] = updateAction(ids, update).effect[F]
    def update(id: I, update: M => M): F[M] = updateAction(id, update).effect[F]
    def deleteAll(): F[Int] = deleteAllAction().effect[F]
  }

}
