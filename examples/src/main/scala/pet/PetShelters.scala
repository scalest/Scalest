package pet

import io.circe.parser._
import io.circe.syntax._
import pet.PetModel.{Address, PetShelter}
import scalest.admin.admin.H2ProfileProvider
import scalest.admin.slick.EntityActions

object PetShelters
  extends EntityActions with H2ProfileProvider {

  import jdbcProfile.api._

  type Id = Int
  type Entity = PetShelter
  type EntityTable = PetShelterTable

  override val idData = IdData(_.id, _.copy(_))

  val petShelters, query = TableQuery[PetShelterTable]

  class PetShelterTable(tag: Tag)
    extends Table[PetShelter](tag, "pet_shelters") with Identified {

    val id = column[Int]("id", O.AutoInc, O.PrimaryKey)

    val name = column[String]("name")

    val address = column[String]("address")

    def read: ((Option[Int], String, String)) => PetShelter = {
      case (a, b, c) => PetShelter(a, b, parse(c).right.get.as[Address].right.get)
    }

    def write: PetShelter => Option[(Option[Int], String, String)] = ps => Option((ps.id, ps.name, ps.address.asJson.noSpaces))

    override def * = (id.?, name, address) <> (read, write)
  }

}
