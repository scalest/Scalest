package pet

import java.util.UUID

import io.circe.Codec
import io.circe.generic.JsonCodec
import pet.PetModel.Genders.Gender

object PetModel {
  def genUUID: String = UUID.randomUUID.toString

  object Genders extends Enumeration {
    type Gender = Value
    val Male = Value("MALE")
    val Female = Value("FEMALE")

    implicit val codec: Codec[Genders.Value] = Codec.codecForEnumeration(this)
  }

  @JsonCodec
  case class Pet(
    id: String,
    name: String,
    adopted: Boolean,
    tags: Seq[String],
    bodySize: Byte,
    gender: Gender,
  )

  object Pet {
    def tupled = (Pet.apply _).tupled
  }

}
