package scalest

import scalest.error._

package object exception {

  abstract class RootException(error: CommonError, cause: Option[Throwable] = None)
      extends RuntimeException(error.error, cause.orNull)

  case class ApplicationException(error: ApplicationError, cause: Option[Throwable]) extends RootException(error, cause)

  object ApplicationException {
    def apply(error: String, cause: Option[Throwable] = None): ApplicationException =
      new ApplicationException(ApplicationError(error), cause)
  }

  case class EntityNotFoundException(error: NotFoundError) extends RootException(error)

  object EntityNotFoundException {
    def apply(error: String): EntityNotFoundException = new EntityNotFoundException(NotFoundError(error))
  }

  case class DatabaseException(error: InternalServerError, cause: Throwable) extends RootException(error, Option(cause))

  object DatabaseException {
    def apply(error: String, cause: Throwable): DatabaseException =
      new DatabaseException(InternalServerError(error), cause)
  }

  case class ModelValidationException(error: ModelValidationError) extends RuntimeException(error.error)

}
