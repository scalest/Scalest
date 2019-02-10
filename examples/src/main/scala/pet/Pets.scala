package pet

import pet.PetModel._
import scalest.admin.admin.H2ProfileProvider
import scalest.admin.slick._

object Pets
  extends EntityActions with H2ProfileProvider {

  import jdbcProfile.api._

  type Id = Int
  type Entity = Pet
  type EntityTable = PetsTable

  override val idData = IdData(_.id, _.copy(_))

  val pets, tableQuery = TableQuery[PetsTable]

  class PetsTable(tag: Tag)
    extends Table[Pet](tag, "pets") with Identified {

    val id = column[Int]("id", O.AutoInc, O.PrimaryKey)

    val name = column[String]("name")

    val adopted = column[Boolean]("adopted")

    override def * = (id.?, name, adopted).mapTo[Pet]
  }

}