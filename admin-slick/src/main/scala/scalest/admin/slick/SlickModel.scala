package scalest.admin.slick

import scalest.admin.slick.DBIOExtensions._
import scalest.admin.slick.exceptions._
import slick.ast.BaseTypedType
import slick.dbio._

import scala.concurrent.ExecutionContext
import scala.language.{existentials, higherKinds, implicitConversions}
import scala.util.{Failure, Success}

abstract class SlickModel { this: JdbcProfileProvider =>

  import jdbcProfile.api._

  type Id

  type Model

  type ModelTable <: SlickModelTable

  val idData: IdData

  private implicit lazy val btt: BaseTypedType[Id] = idData.typed

  private lazy val idLens: Lens[Model, Option[Id]] = idData.idLens

  def query: TableQuery[ModelTable]

  def count: DBIO[Int] = query.size.result

  def findById(id: Id): DBIO[Model] = filterById(id).result.head

  def findOptionById(id: Id): DBIO[Option[Model]] = filterById(id).result.headOption

  def save(m: Model)(implicit exc: ExecutionContext): DBIO[Option[Model]] = (query returning query) insertOrUpdate m

  def create(m: Model)(implicit exc: ExecutionContext): DBIO[Id] = query.returning(query.map(_.id)) += m

  def findAll(implicit exc: ExecutionContext): StreamingDBIO[Seq[Model], Model] = query.result.transactionally

  def delete(m: Model)(implicit exc: ExecutionContext): DBIO[Int] = tryExtractId(m).flatMap(deleteById)

  def deleteById(id: Id)(implicit exc: ExecutionContext): DBIO[Int] = filterById(id).delete.mustAffectOneSingleRow

  def deleteByIds(ids: Set[Id])(implicit exc: ExecutionContext): DBIO[Int] = query.filter(_.id inSet ids).delete

  def update(m: Model)(implicit exc: ExecutionContext): DBIO[Model] = {
    for {
      id <- tryExtractId(m)
      updatedModel <- update(id, m)
    } yield updatedModel
  }

  protected def update(id: Id, m: Model)(implicit exc: ExecutionContext): DBIO[Model] =
    filterById(id)
      .update(m)
      .mustAffectOneSingleRow
      .asTry
      .flatMap {
        case Success(_)                       => DBIO.successful(m)
        case Failure(NoRowsAffectedException) => DBIO.failed(new RowNotFoundException(m))
        case Failure(ex)                      => DBIO.failed(ex)
      }

  private def tryExtractId(m: Model): DBIO[Id] = idLens.get(m) match {
    case Some(id) => SuccessAction(id)
    case None     => FailureAction(new RowNotFoundException(m))
  }

  private def filterById(id: Id) = query.filter(_.id === id)

  //-------------------------------Classes-------------------------------//

  abstract class SlickModelTable(tag: Tag, tableName: String) extends Table[Model](tag, tableName) {
    def id: Rep[Id]
  }

  class IdData(val idLens: Lens[Model, Option[Id]], val typed: BaseTypedType[Id])

  object IdData {
    def apply(get: Model => Option[Id], set: (Model, Option[Id]) => Model)(implicit typed: BaseTypedType[Id]) = new IdData(Lens(get, set), typed)
  }

  //-------------------------------Classes-------------------------------//
}