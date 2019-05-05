package pet

import pet.PetModel.Genders.Gender
import pet.PetModel.{Genders, _}
import scalest.admin.slick._
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import io.circe.syntax._
import io.circe.parser._

object Pets extends SlickModel with H2ProfileProvider {
  import jdbcProfile.api._
  override val data = init

  type Id = Int
  type Model = Pet
  type ModelTable = PetsTable

  val query = TableQuery[PetsTable]

  class PetsTable(tag: Tag) extends SlickModelTable(tag, "pets") {

    val id = column[Int]("id", O.AutoInc, O.PrimaryKey)

    val name = column[String]("name")

    val location = column[Location]("location")

    val bodySize = column[Byte]("body_size")

    val tags = column[Seq[String]]("tags")

    val adopted = column[Boolean]("adopted")

    val gender = column[Gender]("gender")

    override def * = (id.?, name, adopted, tags, location, bodySize, gender).mapTo[Pet]
  }

  def updateAll(pets: Seq[Pet]): DBIOAction[Seq[Int], NoStream, Effect.Write] = DBIO.sequence(pets.map(query.insertOrUpdate))

  implicit val sexEnumMapper: JdbcType[Gender] with BaseTypedType[Gender] = {
    MappedColumnType.base[Gender, String](_.toString, Genders.withName)
  }

  implicit val locationMapper: JdbcType[Location] with BaseTypedType[Location] = {
    MappedColumnType.base[Location, String](_.asJson.noSpaces, parse(_).right.get.as[Location].right.get)
  }

  implicit val seqStringMapper: JdbcType[Seq[String]] with BaseTypedType[Seq[String]] = {
    MappedColumnType.base[Seq[String], String](_.asJson.noSpaces, parse(_).right.get.as[Seq[String]].right.get)
  }

}