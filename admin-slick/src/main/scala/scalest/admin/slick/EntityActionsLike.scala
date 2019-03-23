package scalest.admin.slick

import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

trait EntityActionsLike
  extends CrudActions {

  val jdbcProfile: JdbcProfile

  import jdbcProfile.api._

  type Id

  def create(entity: Model)(implicit exc: ExecutionContext): DBIO[Id]

  def deleteById(id: Id)(implicit exc: ExecutionContext): DBIO[Int]

  def findById(id: Id): DBIO[Model]

  def findOptionById(id: Id): DBIO[Option[Model]]

}
