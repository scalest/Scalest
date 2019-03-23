package scalest.admin.slick

import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext


trait CrudActions {

  val jdbcProfile: JdbcProfile

  import jdbcProfile.api._

  type Model

  def count: DBIO[Int]

  def save(m: Model)(implicit exc: ExecutionContext): DBIO[Option[Model]]

  def update(m: Model)(implicit exc: ExecutionContext): DBIO[Model]

  def delete(m: Model)(implicit exc: ExecutionContext): DBIO[Int]

  def findAll(implicit exc: ExecutionContext): StreamingDBIO[Seq[Model], Model]

}
