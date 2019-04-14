package pet

import pet.PetModel.Sexes.Sex
import scalest.json.{CirceJsonSupport, `E&D`}

object PetModel extends CirceJsonSupport {

  object Sexes
    extends Enumeration {
    type Sex = Value
    val Male = Value("MALE")
    val Female = Value("FEMALE")

    implicit val ed = circeEnum(Sexes)
  }

  case class Pet(id: Option[Int] = None,
                 name: String,
                 adopted: Boolean,
                 tags: Seq[String],
                 bodySize: Byte,
                 sex: Sex)

  object Pet {
    def tupled = (Pet.apply _).tupled

    implicit val ed: `E&D`[Pet] = circeObject
  }

  case class User(id: Option[Int] = None,
                  username: String,
                  password: String)

  object User {
    def tupled = (User.apply _).tupled

    implicit val ed: `E&D`[User] = circeObject
  }

}

