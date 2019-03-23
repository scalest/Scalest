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

  private lazy val idLens: Lens[Model, Option[Id]] = idData.idLens

  trait Identified {
    def id: Rep[Id]
  }

  type EntityTable <: JP#Table[Model] with Identified

  def query: TableQuery[EntityTable]

  override def count: DBIO[Int] = query.size.result

  override def findById(id: Id): DBIO[Model] =
    filterById(id).result.head

  override def findOptionById(id: Id): DBIO[Option[Model]] =
    filterById(id).result.headOption

  override def save(m: Model)(implicit exc: ExecutionContext): DBIO[Option[Model]] = {
    (query returning query) insertOrUpdate m
  }

  override def create(m: Model)(implicit exc: ExecutionContext): DBIO[Id] = {
    query.returning(query.map(_.id)) += m
  }

  override def findAll(implicit exc: ExecutionContext): StreamingDBIO[Seq[Model], Model] = query.result.transactionally

  override def update(m: Model)(implicit exc: ExecutionContext): DBIO[Model] = {
      for {
        id <- tryExtractId(m)
        updatedModel <- update(id, m)
      } yield updatedModel
  }

  protected def update(id: Id, m: Model)(implicit exc: ExecutionContext): DBIO[Model] = {

    val triedUpdate = filterById(id).update(m).mustAffectOneSingleRow.asTry

    triedUpdate.flatMap {
      case Success(_) => DBIO.successful(m)
      case Failure(NoRowsAffectedException) => DBIO.failed(new RowNotFoundException(m))
      case Failure(ex) => DBIO.failed(ex)
    }

  }

  override def delete(m: Model)(implicit exc: ExecutionContext): DBIO[Int] = {
    tryExtractId(m).flatMap { id =>
      deleteById(id)
    }
  }

  def deleteById(id: Id)(implicit exc: ExecutionContext): DBIO[Int] = {
    filterById(id).delete.mustAffectOneSingleRow
  }

  def deleteByIds(ids: Set[Id])(implicit exc: ExecutionContext): DBIO[Int] = {
    query.filter(_.id inSet ids).delete
  }

  private def tryExtractId(m: Model): DBIO[Id] = {
    idLens.get(m) match {
      case Some(id) => SuccessAction(id)
      case None => FailureAction(new RowNotFoundException(m))
    }
  }

  private def filterById(id: Id) = query.filter(_.id === id)

  class IdData(val idLens: Lens[Model, Option[Id]], val typed: BaseTypedType[Id])

  object IdData {
    def apply(get: Model => Option[Id], set: (Model, Option[Id]) => Model)
             (implicit typed: BaseTypedType[Id]) = new IdData(Lens(get, set), typed)
  }

}