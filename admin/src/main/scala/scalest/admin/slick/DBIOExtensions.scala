package scalest.admin.slick

import scalest.admin.slick.exceptions._
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

object DBIOExtensions {

  implicit class UpdateActionExtensionMethods(dbAction: DBIO[Int]) {

    def mustAffectOneSingleRow(implicit exc: ExecutionContext): DBIO[Int] = {
      dbAction.flatMap {
        case 1 => DBIO.successful(1) // expecting one result
        case 0 => DBIO.failed(NoRowsAffectedException)
        case n if n > 1 => DBIO.failed(new TooManyRowsAffectedException(affectedRowCount = n, expectedRowCount = 1))
      }
    }

  }


}
