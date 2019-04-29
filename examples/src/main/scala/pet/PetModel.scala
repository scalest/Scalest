package pet

import pet.PetModel.Genders.Gender
import scalest.admin.{Component, FieldTypeSchema}
import scalest.json.{CirceJsonSupport, JsonConverter}
import cats.syntax.option._
import io.circe.syntax._

object PetModel extends CirceJsonSupport {

  object Genders extends Enumeration {
    type Gender = Value
    val Male = Value("MALE")
    val Female = Value("FEMALE")

    implicit val jc = circeEnum(Genders)
  }

  case class Location(latitude: Double, longitude: Double)

  object Location {
    implicit val jc: JsonConverter[Location] = circeObject[Location]

    val inputComponent = Component(
      "location-input",
      """
        | const c = {
        |   name: 'location-input',
        |   props: {field: Object, item: Object},
        |   template: '<v-text-field v-model="item[field.name]" box :label="field.name"></v-text-field>'
        | };
        | c;
      """.stripMargin
      )

    val outputComponent = Component(
      "location-output",
      """
        | const c = {
        |   name: "location-output",
        |   props: {field: Object, item: Object},
        |   template: '<span>{{item[field.name]}}</span>'
        | };
        | c;
      """.stripMargin
      )

    implicit val locationFTS: FieldTypeSchema[Location] = FieldTypeSchema(
      inputComponent = inputComponent.some,
      outputComponent = outputComponent.some,
      default = Location(0, 0).asJson.some)
  }

  case class Pet(id: Option[Int] = None,
                 name: String,
                 adopted: Boolean,
                 tags: Seq[String],
                 location: Location,
                 bodySize: Byte,
                 gender: Gender)

  object Pet {
    def tupled = (Pet.apply _).tupled

    implicit val jc: JsonConverter[Pet] = circeObject
  }

  case class User(id: Option[Int] = None,
                  username: String,
                  password: String)

  object User {
    def tupled = (User.apply _).tupled

    implicit val jc: JsonConverter[User] = circeObject
  }

}

