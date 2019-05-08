package pet

import pet.PetModel.User
import slick.jdbc.H2Profile.api._

object Users {
  val users = TableQuery[UsersTable]

  class UsersTable(tag: Tag) extends Table[User](tag, "users") {

    val id = column[String]("id", O.PrimaryKey)

    val username = column[String]("username")

    val password = column[String]("passwrod")

    override def * = (id, username, password).mapTo[User]
  }

}
