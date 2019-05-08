package pet

import pet.PetModel.{User, UserQuery}
import pet.Users.{users, UsersTable}
import scalest.admin.FutureToEffect
import slick.basic.DatabaseConfig
import scala.concurrent.ExecutionContext
import slick.jdbc.H2Profile

class UserRepository[F[_]: FutureToEffect](implicit val dc: DatabaseConfig[H2Profile], val ec: ExecutionContext)
    extends SlickCrudRepository[F, User, UserQuery, UsersTable] {
  import dc.profile.api._

  override val tableQuery: TableQuery[UsersTable] = users
  override val queryFactory: ModelQueryFactory[UserQuery] = ModelQueryFactory.gen[UserQuery](_.id)
  override def findByQuery(query: UserQuery): Query[UsersTable, User, Seq] = searchQuery(query, Nil)
}
