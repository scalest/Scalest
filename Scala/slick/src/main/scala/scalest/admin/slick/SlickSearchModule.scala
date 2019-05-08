package scalest.admin.slick

import cats.implicits._
import io.circe.Decoder.Result
import io.circe.Json._
import io.circe._
import io.circe.syntax._
import javax.naming.OperationNotSupportedException
import scalest.admin.slick.meta.ModelQueryFactoryMacros
import slick.jdbc.{JdbcProfile, JdbcType}
import sttp.tapir.SchemaType.SObjectInfo
import sttp.tapir.{Schema, SchemaType, Validator}

import scala.language.experimental.macros

trait SlickSearchModule {
  type Profile <: JdbcProfile
  val profile: Profile
  import profile.api.{stringColumnType => _, _}

  trait ModelQuery {
    type Id
    def id: SearchParam[Id]
  }

  trait ModelQueryFactory[Q <: ModelQuery] {
    def fromId(id: Q#Id): Q
    def fromIds(ids: Seq[Q#Id]): Q
    def empty: Q
  }

  object ModelQueryFactory {
    def gen[Q <: ModelQuery](hintP: Q => SearchParam[Q#Id]): ModelQueryFactory[Q] = macro ModelQueryFactoryMacros.impl[Q]
  }

  sealed abstract class SearchParam[T](val operation: Operations.Operation) {
    def interpret(field: Rep[T])(implicit jdbcType: JdbcType[T]): Rep[Boolean]
  }

  object SearchParam {
    private def decodeLogicalOperation[T: Decoder](c: HCursor): Decoder.Result[(SearchParam[T], SearchParam[T])] = {
      for {
        first <- c.get[SearchParam[T]]("first")
        second <- c.get[SearchParam[T]]("second")
      } yield (first, second)
    }

    implicit def decoder[T: Decoder]: Decoder[SearchParam[T]] = Decoder.instance { c =>
      if(c.value.isNull) EmptyParam[T]().asRight
      else c.get[Operations.Operation]("operation").flatMap {
        case Operations.Custom => DecodingFailure("custom search param not supported", c.history).asLeft
        case Operations.And    => decodeLogicalOperation[T](c).map((AndParam.apply[T] _).tupled)
        case Operations.Or     => decodeLogicalOperation[T](c).map((OrParam.apply[T] _).tupled)
        case Operations.Empty  => EmptyParam[T]().asRight
        case Operations.InSet  => c.get[Iterable[T]]("values").map(InSetParam(_))
        case Operations.Like   => c.get[String]("value").map(StringParam(_, Operations.Like)).asInstanceOf[Result[SearchParam[T]]]
        case o                 => c.get[T]("value").map(OperationParam[T](_, o))
      }
    }
    implicit def encoder[T: Encoder]: Encoder[SearchParam[T]] = Encoder.instance {
      case p: EmptyParam[T]   => obj("operation" -> p.operation.asJson)
      case p: InSetParam[T]   => obj("operation" -> p.operation.asJson, "values" -> p.values.asJson)
      case p: SingleParam[T]  => obj("operation" -> p.operation.asJson, "value" -> p.value.asJson)
      case p: LogicalParam[T] => obj("operation" -> p.operation.asJson, "first" -> p.first.asJson(encoder), "seconds" -> p.second.asJson(encoder))
      case _                  => throw new OperationNotSupportedException()
    }
    implicit def schema[T]: Schema[SearchParam[T]] = Schema(
      SchemaType.SCoproduct(SObjectInfo("SearchParam", List.empty), List.empty, None)
    )
    implicit def validator[T]: Validator[SearchParam[T]] = Validator.pass
  }

  case class EmptyParam[T]() extends SearchParam[T](Operations.Empty) {
    def interpret(field: Rep[T])(implicit jdbcType: JdbcType[T]): Rep[Boolean] = true
  }

  object Operations extends Enumeration {
    type Operation = Value
    val Equals: Operation = Value("EQUALS")
    val NotEquals: Operation = Value("NOT_EQUALS")
    val Greater: Operation = Value("GREATER")
    val Less: Operation = Value("LESS")
    val GreaterEqual: Operation = Value("GREATER_EQUAL")
    val LessEqual: Operation = Value("LESS_EQUAL")
    val Like: Operation = Value("LIKE")
    val And: Operation = Value("AND")
    val Or: Operation = Value("OR")
    val Custom: Operation = Value("CUSTOM")
    val InSet: Operation = Value("IN_SET")
    val Empty: Operation = Value("EMPTY")
    implicit val codec: Codec[Operation] = Codec.codecForEnumeration(this)
  }

  case class CustomParam[T](impl: JdbcType[T] => Rep[T] => Rep[Boolean]) extends SearchParam[T](Operations.Custom) {
    override def interpret(field: Rep[T])(implicit jdbcType: JdbcType[T]): Rep[Boolean] = impl(jdbcType)(field)
  }

  sealed trait SingleParam[T] {
    def operation: Operations.Operation
    def value: T
  }

  case class OperationParam[T](value: T, op: Operations.Operation) extends SearchParam[T](op) with SingleParam[T] {
    def interpret(field: Rep[T])(implicit jdbcType: JdbcType[T]): Rep[Boolean] = operation match {
      case Operations.Equals       => field === value
      case Operations.NotEquals    => field =!= value
      case Operations.Greater      => field > value
      case Operations.GreaterEqual => field >= value
      case Operations.Less         => field < value
      case Operations.LessEqual    => field <= value
    }
  }

  case class StringParam(value: String, op: Operations.Operation) extends SearchParam[String](op) with SingleParam[String] {
    def interpret(field: Rep[String])(implicit jdbcType: JdbcType[String]): Rep[Boolean] = operation match {
      case Operations.Like => field like value
    }
  }

  case class InSetParam[T](values: Iterable[T]) extends SearchParam[T](Operations.InSet) {
    def interpret(field: Rep[T])(implicit jdbcType: JdbcType[T]): Rep[Boolean] = field inSet values
  }

  sealed trait LogicalParam[T] {
    def operation: Operations.Operation
    def first: SearchParam[T]
    def second: SearchParam[T]
  }

  case class AndParam[T](first: SearchParam[T], second: SearchParam[T]) extends SearchParam[T](Operations.And) with LogicalParam[T] {
    def interpret(field: Rep[T])(implicit jdbcType: JdbcType[T]): Rep[Boolean] = first.interpret(field) && second.interpret(field)
  }

  case class OrParam[T](first: SearchParam[T], second: SearchParam[T]) extends SearchParam[T](Operations.Or) with LogicalParam[T] {
    def interpret(field: Rep[T])(implicit jdbcType: JdbcType[T]): Rep[Boolean] = first.interpret(field) || second.interpret(field)
  }

  def emptyParam[T]: SearchParam[T] = EmptyParam()
  def custom[T](impl: Rep[T] => Rep[Boolean]): SearchParam[T] = CustomParam(_ => impl)
  def customParam[T](impl: JdbcType[T] => Rep[T] => Rep[Boolean]): SearchParam[T] = CustomParam(impl)
  def like(value: String): SearchParam[String] = StringParam(value, Operations.Like)
  def eqs[T](value: T): SearchParam[T] = OperationParam(value, Operations.Equals)
  def neq[T](value: T): SearchParam[T] = OperationParam(value, Operations.NotEquals)
  def lt[T](value: T): SearchParam[T] = OperationParam(value, Operations.Less)
  def lte[T](value: T): SearchParam[T] = OperationParam(value, Operations.LessEqual)
  def gt[T](value: T): SearchParam[T] = OperationParam(value, Operations.Greater)
  def gte[T](value: T): SearchParam[T] = OperationParam(value, Operations.GreaterEqual)
  def inSet[T](value: Iterable[T]): SearchParam[T] = InSetParam(value)

  implicit class SearchParamOps[T](value: SearchParam[T]) {
    def and(other: SearchParam[T]): SearchParam[T] = AndParam(value, other)
    def or(other: SearchParam[T]): SearchParam[T] = OrParam(value, other)
  }

}
