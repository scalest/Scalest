package pet

import pet.PetModel.User
import scalest.admin.slick.{H2ProfileProvider, SlickModel}


object Users extends SlickModel with H2ProfileProvider {
  import jdbcProfile.api._
  override val data = init

  type Id = Int
  type Model = User
  type ModelTable = UsersTable

  val query = TableQuery[UsersTable]

  class UsersTable(tag: Tag) extends SlickModelTable(tag, "users") {

    val id = column[Int]("id", O.AutoInc, O.PrimaryKey)

    val username = column[String]("username")

    val password = column[String]("passwrod")

    override def * = (id.?, username, password).mapTo[User]
  }

}