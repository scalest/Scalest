package pet

import io.circe.Codec
import io.circe.generic.semiauto._
import pet.PetModel.Genders.Gender
import java.{util => ju}

object PetModel {
  val uuidGenerator = () => ju.UUID.randomUUID.toString

  object Genders extends Enumeration {
    type Gender = Value
    val Male = Value("MALE")
    val Female = Value("FEMALE")

    implicit val codec: Codec[Genders.Value] = Codec.codecForEnumeration(this)
  }

  case class Pet(
    id: String,
    name: String,
    adopted: Boolean,
    tags: Seq[String],
    bodySize: Byte,
    gender: Gender
  )

  object Pet {
    def tupled = (Pet.apply _).tupled
    implicit val codec: Codec[Pet] = deriveCodec
  }

  case class PetQuery(id: SearchParam[String] = EmptyParam(), name: Option[String] = None) extends ModelQuery {
    override type Id = String
  }

  object PetQuery {
    implicit val codec: Codec[PetQuery] = deriveCodec
  }

  case class User(id: String, username: String, password: String)

  object User {
    def tupled = (User.apply _).tupled

    implicit val codec: Codec[User] = deriveCodec
  }

  case class UserQuery(id: SearchParam[String] = EmptyParam(), username: Option[String] = None) extends ModelQuery {
    override type Id = String
  }

  object UserQuery {
    implicit val codec: Codec[UserQuery] = deriveCodec
  }

}
