package scalest.admin.slick

import slick.ast.BaseTypedType

import scala.language.{existentials, higherKinds, implicitConversions}

abstract class SlickModel { this: JdbcProfileProvider =>

  import jdbcProfile.api._

  type Id

  type Model

  type ModelTable <: SlickModelTable

  implicit val data: BaseTypedType[Id]

  def query: TableQuery[ModelTable]

  def count: DBIO[Int] = query.size.result

  def findById(id: Id): DBIO[Model] = filterById(id).result.head

  def findOptionById(id: Id): DBIO[Option[Model]] = filterById(id).result.headOption

  def findByIds(ids: Set[Id]): DBIO[Seq[Model]] = query.filter(_.id inSet ids).result

  def findAll: StreamingDBIO[Seq[Model], Model] = query.result.transactionally

  def save(m: Model): DBIO[Int] = query insertOrUpdate m

  def create(m: Model): DBIO[Id] = (query returning query.map(_.id)) += m

  def deleteById(id: Id): DBIO[Int] = filterById(id).delete

  def deleteByIds(ids: Set[Id]): DBIO[Int] = query.filter(_.id inSet ids).delete

  private def filterById(id: Id) = query.filter(_.id === id)

  //-------------------------------Classes-------------------------------//

  abstract class SlickModelTable(tag: Tag, tableName: String) extends Table[Model](tag, tableName) {
    def id: Rep[Id]
  }

  def init(implicit idTyped: BaseTypedType[Id]): BaseTypedType[Id] = idTyped

  //-------------------------------Classes-------------------------------//
}