package scalest.service

import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredCodec
import io.circe.parser.parse
import io.circe.syntax._
import io.circe.{Codec, Json}

sealed trait SearchDsl

object SearchDsl extends App {
  implicit private val configuration: Configuration =
    Configuration.default
      .copy(
        transformConstructorNames = {
          case "And"          => "and"
          case "Or"           => "or"
          case "Equal"        => "eq"
          case "NotEqual"     => "neq"
          case "Greater"      => "gt"
          case "Less"         => "lt"
          case "GreaterEqual" => "gte"
          case "LessEqual"    => "lte"
        },
      )
  implicit val codec: Codec[SearchDsl] = deriveConfiguredCodec

  case class And(left: SearchDsl, right: SearchDsl) extends SearchDsl
  case class Or(left: SearchDsl, right: SearchDsl) extends SearchDsl

  sealed trait FieldSearchDsl extends SearchDsl {
    def field: String
    def value: Json
  }
  case class Equal(field: String, value: Json) extends FieldSearchDsl
  case class NotEqual(field: String, value: Json) extends FieldSearchDsl
  case class Greater(field: String, value: Json) extends FieldSearchDsl
  case class Less(field: String, value: Json) extends FieldSearchDsl
  case class GreaterEqual(field: String, value: Json) extends FieldSearchDsl
  case class LessEqual(field: String, value: Json) extends FieldSearchDsl

  println((And(Equal("name", "oleg".asJson), GreaterEqual("age", 18.asJson)): SearchDsl).asJson)

  val json = """
               |{
               |  "and" : {
               |    "left" : {
               |      "eq" : {
               |        "field" : "name",
               |        "value" : "oleg"
               |      }
               |    },
               |    "right" : {
               |      "gte" : {
               |        "field" : "age",
               |        "value" : 18
               |      }
               |    }
               |  }
               |}
               |""".stripMargin

  private val option = parse(json).flatMap(_.as[SearchDsl])
  println(option)

}
