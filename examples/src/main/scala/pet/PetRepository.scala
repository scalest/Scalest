package pet

import akka.actor.ActorSystem
import pet.PetModel._
import scalest.admin.CrudRepository
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile

import scala.concurrent.Future

class PetRepository(system: ActorSystem,
                    dc: DatabaseConfig[H2Profile])
  extends CrudRepository[Pet] {

  import dc.profile.api._
  import system._

  def execute[R](action: DBIOAction[R, NoStream, Nothing]): Future[R] = dc.db.run(action)

  def findAll(): Future[Seq[Pet]] = execute(Pets.fetchAll())

  def create(pet: Pet): Future[Int] = execute(Pets.insert(pet))

  def upsert(pet: Pet): Future[Pet] = execute(Pets.update(pet))

  def delete(id: Int): Future[Int] = execute(Pets.deleteById(id))

  def deleteAll(ids: Seq[Int]): Future[Int] = execute(Pets.deleteByIds(ids.toSet))
}
