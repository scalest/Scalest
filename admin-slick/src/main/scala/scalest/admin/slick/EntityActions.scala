package scalest.admin.slick

import scalest.admin.slick.DBIOExtensions._
import scalest.admin.slick.exceptions._
import slick.ast.BaseTypedType
import slick.dbio._

import scala.concurrent.ExecutionContext
import scala.language.{existentials, higherKinds, implicitConversions}
import scala.util.{Failure, Success}

abstract class EntityActions
  extends EntityActionsLike { this: JdbcProfileProvider =>

  import jdbcProfile.api._

  val idData: IdData

  private implicit lazy val btt: BaseTypedType[Id] = idData.typed

  private lazy val idLens: Lens[Entity, Option[Id]] = idData.idLens

  trait Identified {
    def id: Rep[Id]
  }

  type EntityTable <: JP#Table[Entity] with Identified

  def tableQuery: TableQuery[EntityTable]

  override def count: DBIO[Int] = tableQuery.size.result

  override def findById(id: Id): DBIO[Entity] =
    filterById(id).result.head

  override def findOptionById(id: Id): DBIO[Option[Entity]] =
    filterById(id).result.headOption

  override def save(entity: Entity)(implicit exc: ExecutionContext): DBIO[Entity] = {
    idLens.get(entity) match {
      // if has an Id, try to update it
      case Some(_) => update(entity)

      // if has no Id, try to add it
      case None => insert(entity).map { id =>
        idLens.set(entity, Option(id))
      }
    }
  }

  def beforeInsert(entity: Entity)(implicit exc: ExecutionContext): DBIO[Entity] = {
    // default implementation does nothing
    DBIO.successful(entity)
  }


  def beforeUpdate(id: Id, entity: Entity)(implicit exc: ExecutionContext): DBIO[Entity] = {
    // default implementation does nothing
    DBIO.successful(entity)
  }

  override def insert(entity: Entity)(implicit exc: ExecutionContext): DBIO[Id] = {
    val action = beforeInsert(entity).flatMap { preparedModel =>
      tableQuery.returning(tableQuery.map(_.id)) += preparedModel
    }
    // beforeInsert and '+=' must run on same tx
    action.transactionally
  }

  override def fetchAll(fetchSize: Int = 100)(implicit exc: ExecutionContext): StreamingDBIO[Seq[Entity], Entity] = {
    tableQuery
      .result
      .transactionally
      .withStatementParameters(fetchSize = fetchSize)
  }

  override def update(entity: Entity)(implicit exc: ExecutionContext): DBIO[Entity] = {
    val action =
      for {
        id <- tryExtractId(entity)
        preparedModel <- beforeUpdate(id, entity)
        updatedModel <- update(id, preparedModel)
      } yield updatedModel

    // beforeUpdate and update must run on same tx
    action.transactionally
  }


  protected def update(id: Id, entity: Entity)(implicit exc: ExecutionContext): DBIO[Entity] = {

    val triedUpdate = filterById(id).update(entity).mustAffectOneSingleRow.asTry

    triedUpdate.flatMap {
      case Success(_) => DBIO.successful(entity)
      case Failure(NoRowsAffectedException) => DBIO.failed(new RowNotFoundException(entity))
      case Failure(ex) => DBIO.failed(ex)
    }

  }

  override def delete(entity: Entity)(implicit exc: ExecutionContext): DBIO[Int] = {
    tryExtractId(entity).flatMap { id =>
      deleteById(id)
    }
  }

  def deleteById(id: Id)(implicit exc: ExecutionContext): DBIO[Int] = {
    filterById(id).delete.mustAffectOneSingleRow
  }

  def deleteByIds(ids: Set[Id])(implicit exc: ExecutionContext): DBIO[Int] = {
    tableQuery.filter(_.id inSet ids).delete
  }

  private def tryExtractId(entity: Entity): DBIO[Id] = {
    idLens.get(entity) match {
      case Some(id) => SuccessAction(id)
      case None => FailureAction(new RowNotFoundException(entity))
    }
  }

  private def filterById(id: Id) = tableQuery.filter(_.id === id)

  //Helper class that provides needed entities
  class IdData(val idLens: Lens[Entity, Option[Id]], val typed: BaseTypedType[Id])

  object IdData {
    def apply(get: Entity => Option[Id], set: (Entity, Option[Id]) => Entity)
             (implicit typed: BaseTypedType[Id]) = new IdData(
      Lens(
        get,
        set
      ), typed
    )
  }

}