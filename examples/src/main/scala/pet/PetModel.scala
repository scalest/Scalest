package pet

import scalest.admin.slick.ActiveRecord
import scalest.json.{CirceJsonSupport, `E&D`}

object PetModel
  extends CirceJsonSupport {

  case class Pet(id: Option[Int] = None, name: String, adopted: Boolean)

  object Pet {
    def tupled = (Pet.apply _).tupled

    implicit val ed: `E&D`[Pet] = circeObject[Pet]

    implicit class EntryExtensions(val model: Pet)
      extends ActiveRecord(Pets)

  }

}
