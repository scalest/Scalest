package pet

import pet.PetModel.Sexes.Sex
import pet.PetModel.{Sexes, _}
import scalest.admin.admin.H2ProfileProvider
import scalest.admin.slick._
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType

object Pets
  extends EntityActions with H2ProfileProvider {

  import jdbcProfile.api._

  type Id = Int
  type Entity = Pet
  type EntityTable = PetsTable

  override val idData = IdData(_.id, _.copy(_))

  val pets, tableQuery = TableQuery[PetsTable]

  implicit val sexEnumMapper: JdbcType[Sex] with BaseTypedType[Sex] = {
    MappedColumnType.base[Sex, String](_.toString, Sexes.withName)
  }

  class PetsTable(tag: Tag)
    extends Table[Pet](tag, "pets") with Identified {

    val id = column[Int]("id", O.AutoInc, O.PrimaryKey)

    val name = column[String]("name")

    val adopted = column[Boolean]("adopted")

    val sex = column[Sex]("sex")

    override def * = (id.?, name, adopted, sex).mapTo[Pet]
  }

}