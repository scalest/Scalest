package pet

import pet.PetModel.User
import scalest.admin.admin.H2ProfileProvider
import scalest.admin.slick.SlickModel


object Users extends SlickModel with H2ProfileProvider {

  import jdbcProfile.api._

  type Id = Int
  type Model = User
  type ModelTable = UsersTable

  override val idData = IdData(_.id, _.copy(_))

  val query = TableQuery[UsersTable]

  class UsersTable(tag: Tag) extends SlickModelTable(tag, "users") {

    val id = column[Int]("id", O.AutoInc, O.PrimaryKey)

    val username = column[String]("username")

    val password = column[String]("passwrod")

    override def * = (id.?, username, password).mapTo[User]
  }

}