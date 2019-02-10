package scalest.error

trait ErrorMessages {
  final val InternalErrorMessage = "error.internal"
  final val ValidationErrorMessage = "error.validation"
  final val EntityNotFoundErrorMessage = "error.entity-not-found"
  final val NonUniqueResultErrorMessage = "error.non-unique-result"
  final val MissingFieldErrorMessage = "error.field-missing"
  final val MalformedFieldErrorMessage = "error.field-malformed"
  final val AuthFailedErrorMessage = "error.auth-failed"
  final val NotImplementedErrorMessage = "error.not-implemented"
}
