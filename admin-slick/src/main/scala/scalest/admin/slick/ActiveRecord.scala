package scalest.admin.slick

import slick.dbio.DBIO

import scala.concurrent.ExecutionContext


abstract class ActiveRecord[R <: CrudActions](val repository: R) {

  def model: repository.Entity

  def save()(implicit exc: ExecutionContext): DBIO[repository.Entity] =
    repository.save(model)

  def update()(implicit exc: ExecutionContext): DBIO[repository.Entity] =
    repository.update(model)

  def delete()(implicit exc: ExecutionContext): DBIO[Int] = repository.delete(model)

}
