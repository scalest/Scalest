package scalest

import cats.data._
import cats.implicits._
import io.circe.Codec
import scalest.error._
import scalest.validation.ValidationTypes.ValidationType

import scala.language.experimental.macros
import scala.language.implicitConversions
import scala.util.Try

/**
 * Todo: Add schema to validators that can be used on FE to create validations
 * Todo: Add interop to tapir validation
 */
package object validation {
  final val EmptyFieldError = "error.field-empty"
  final val NotEmptyFieldError = "error.field-not-empty"
  final val FieldMaxValueError = "error.validation.field.max.value"
  final val FieldMinValueError = "error.validation.field.min.value"

  type ValidationResult = ValidatedNel[FieldValidation, Unit]

  val Valid: ValidationResult = ().validNel[FieldValidation]

  object ValidationTypes extends Enumeration {
    type ValidationType = Value
    val Min: ValidationType = Value("MIN")
    val Max: ValidationType = Value("MAX")
    val Custom: ValidationType = Value("CUSTOM")
    implicit val codec: Codec[ValidationType] = Codec.codecForEnumeration(this)
  }

  trait Validator[T] {
    def schema: ValidationSchema
    def validate(el: T): ValidatedNel[FieldValidation, T]
  }

  case class Field[T](name: String, value: T, parent: Option[Field[_]] = None) {
    def fullName: String = parent.map(_.fullName + s".$name").getOrElse(name)
  }

  case class ValidationSchema(path: String,
                              `type`: ValidationType,
                              description: String)

  case class FieldValidation(fieldName: String, fieldError: String)

  object FieldValidation {
    def apply(field: Field[_], fieldError: String): FieldValidation = FieldValidation(field.fullName, fieldError)
  }

  def field[A](value: A): Field[A] = macro FieldMacros.fieldMacro[A]

  def fieldWithParent[A](value: A, parent: Field[_]): Field[A] = macro FieldMacros.fieldWithParentMacro[A]

  implicit class FieldOps[A](val field: Field[A]) extends AnyVal {
    def map[B](f: A => B): Field[B] = Field(field.name, f(field.value), field.parent)

    def invalid(error: String): ValidationResult = FieldValidation(field, error).invalidNel

    def child[B](function: A => B): Field[B] = macro FieldMacros.childMacro[A, B]

    def child[B](name: String, value: B): Field[B] = Field(name, value, Some(field))
  }

  implicit def fieldConversion[A, B](field: Field[A])(implicit f: A => B): Field[B] = {
    import field._
    Field(name, f(value), parent)
  }

  def condNelValid(cond: Boolean)(error: ValidationResult): ValidationResult = if (cond) Valid else error

  def condNelInvalid(cond: Boolean)(error: ValidationResult): ValidationResult = if (cond) error else Valid

  def validateMin[A](field: Field[A], minValue: A)(implicit ordering: Ordering[A]): ValidationResult = {
    import ordering._
    condNelValid(field.value >= minValue) {
      field.invalid(FieldMinValueError)
    }
  }

  def validateMax[A](field: Field[A], maxValue: A)(implicit ordering: Ordering[A]): ValidationResult = {
    import ordering._
    condNelValid(field.value <= maxValue) {
      field.invalid(FieldMaxValueError)
    }
  }

  def validateMinLength[T](field: Field[Iterable[T]], min: Int): ValidationResult = {
    condNelValid(field.value.size >= min) {
      field.invalid(FieldMinValueError)
    }
  }

  def validateMaxLength[T](field: Field[Iterable[T]], max: Int): ValidationResult = {
    condNelValid(field.value.size <= max) {
      field.invalid(FieldMaxValueError)
    }
  }

  def validateSeq[T](field: Field[Seq[T]])(validation: (Field[T], Int) => ValidationResult): ValidationResult = {
    field.value.zipWithIndex.foldLeft(Valid) { (prev, indexed) =>
      val (c, index) = indexed
      prev |+| validation(field.child(index.toString, c), index)
    }
  }

  def validateSeq[T](field: Field[Seq[T]], validation: Field[T] => ValidationResult): ValidationResult = {
    validateSeq[T](field)((f, _) => validation(f))
  }

  def validateEmpty[T](field: Field[Seq[T]]): ValidationResult = {
    condNelValid(field.value.nonEmpty) {
      field.invalid(EmptyFieldError)
    }
  }

  def validateEnum(field: Field[String], enum: Enumeration): ValidationResult = {
    Try(enum.withName(field.value))
      .toOption
      .map(_ => Valid)
      .getOrElse(field.invalid(MalformedFieldErrorMessage))
  }

  def validateJEnum[T <: Enum[T]](field: Field[String], values: Array[T]): ValidationResult = {
    condNelValid(values.map(_.name()).contains(field.value)) {
      field.invalid(MalformedFieldErrorMessage)
    }
  }

  def multiFieldsError(fields: Seq[Field[_]], error: String): ValidationResult = {
    fields.map(FieldValidation(_, error).invalidNel).foldLeft(Valid) { (l, r) => l |+| r }
  }

  def lift2[A, B](f: Field[A] => ValidationResult): Field[Option[A]] => ValidationResult = {
    fA => fA.value.map(value => f(Field(fA.name, value, fA.parent))).getOrElse(().validNel)
  }

  def lift3[A, B, C](f: (Field[A], B) => ValidationResult): (Field[Option[A]], B) => ValidationResult = {
    (fA, b) => fA.value.map(value => f(Field(fA.name, value, fA.parent), b)).getOrElse(().validNel)
  }

  def collectErrors(errors: NonEmptyList[FieldValidation]): ModelValidationError = {
    errors.foldLeft(ModelValidationError(ValidationErrorMessage)) { (apiError, fieldValidation) =>
      apiError.copy(fields = apiError.fields + (fieldValidation.fieldName -> FieldValidationError(fieldValidation.fieldError)))
    }
  }
}
