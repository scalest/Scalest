package pet

import pet.PetModel.{Pet, PetQuery}
import pet.Pets.{pets, PetsTable}
import scalest.admin.FutureToEffect
import slick.basic.DatabaseConfig
import scala.concurrent.ExecutionContext
import slick.jdbc.H2Profile

class PetRepository[F[_]: FutureToEffect](implicit val dc: DatabaseConfig[H2Profile], val ec: ExecutionContext)
    extends SlickCrudRepository[F, Pet, PetQuery, PetsTable] {
  import dc.profile.api._

  override val tableQuery: TableQuery[PetsTable] = pets
  override val queryFactory: ModelQueryFactory[PetQuery] = ModelQueryFactory.gen[PetQuery](_.id)
  override def findByQuery(query: PetQuery): Query[PetsTable, Pet, Seq] = searchQuery(query, Nil)
}
