package pet

import io.circe.parser._
import io.circe.syntax._
import pet.PetModel.Genders.Gender
import pet.PetModel.{Genders, _}
import slick.ast.BaseTypedType
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcType

object Pets {
  val pets = TableQuery[PetsTable]

  class PetsTable(tag: Tag) extends Table[Pet](tag, "pets") {

    val id = column[String]("id", O.PrimaryKey)

    val name = column[String]("name")

    val bodySize = column[Byte]("body_size")

    val tags = column[Seq[String]]("tags")

    val adopted = column[Boolean]("adopted")

    val gender = column[Gender]("gender")

    override def * = (id, name, adopted, tags, bodySize, gender).mapTo[Pet]
  }

  implicit val sexEnumMapper: JdbcType[Gender] with BaseTypedType[Gender] = {
    MappedColumnType.base[Gender, String](_.toString, Genders.withName)
  }

  implicit val seqStringMapper: JdbcType[Seq[String]] with BaseTypedType[Seq[String]] = {
    MappedColumnType.base[Seq[String], String](_.asJson.noSpaces, parse(_).right.get.as[Seq[String]].right.get)
  }

}
