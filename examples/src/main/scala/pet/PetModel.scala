package pet

import scalest.admin.NoWrite
import scalest.json.{CirceJsonSupport, `E&D`}

object PetModel
  extends CirceJsonSupport {

  case class Address(avenue: String, house: Int, floor: String, apartments: Int)

  object Address {
    implicit val ed: `E&D`[Address] = circeObject
  }

  case class PetShelter(id: Option[Int] = None, name: String, address: Address)

  object PetShelter {
    def tupled = (PetShelter.apply _).tupled

    implicit val ed: `E&D`[PetShelter] = circeObject
  }

  object Sexes
    extends Enumeration {
    type Sex = Value
    val Male = Value("MALE")
    val Female = Value("FEMALE")

    implicit val ed = circeEnum(Sexes)
  }

  case class Pet(@NoWrite
                 id: Option[Int] = None,
                 name: String,
                 adopted: Boolean,
                 tags: Seq[String],
                 bodySize: Byte,
                 sex: Sexes.Value)

  object Pet {
    def tupled = (Pet.apply _).tupled

    implicit val ed: `E&D`[Pet] = circeObject
  }

}

