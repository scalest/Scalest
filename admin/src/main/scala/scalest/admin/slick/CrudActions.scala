package scalest.admin.slick

import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

/**
  * Define basic CRUD actions.
  *
  * This trait make no assumption about the presence of an Entity and a corresponding ID.
  * Therefore it can also be used for persistence of Value Objects.
  */
trait CrudActions {

  val jdbcProfile: JdbcProfile

  import jdbcProfile.api._

  type Entity

  /** Returns total table count */
  def count: DBIO[Int]

  /** Insert or Update a Model
    * Insert `Model` if not yet persisted, otherwise update it.
    *
    * @return DBIO[Model] for a `Model` as persisted in the table.
    */
  def save(entity: Entity)(implicit exc: ExecutionContext): DBIO[Entity]

  /** Update a `Model`.
    *
    * @return DBIO[Model] for a `Model` as persisted in the table.
    */
  def update(entity: Entity)(implicit exc: ExecutionContext): DBIO[Entity]

  /** Delete a `Model`.
    *
    * @return DBIO[Int] with the number of affected rows
    */
  def delete(entity: Entity)(implicit exc: ExecutionContext): DBIO[Int]

  /** Fetch all elements from a table.
    *
    * @param fetchSize - the number of row to fetch, defaults to 100
    * @return StreamingDBIO[Seq[Model], Model]
    */
  def fetchAll(fetchSize: Int = 100)
              (implicit exc: ExecutionContext): StreamingDBIO[Seq[Entity], Entity]

  // end::adoc[]
}
