package pet

import pet.PetModel.Pet
import scalest.admin._
import scalest.admin.akka.AkkaModelAdmin
import scalest.admin.slick._

import scala.concurrent.Future

trait PetsModelAdmin { self: PetEnvironment =>
  val petsMA: AkkaModelAdmin[Pet, Int] = AkkaModelAdmin(SlickRepository(Pets), Set(Adopt))

  def Adopt: ModelAction[Pet, Int] = new ModelAction[Pet, Int] {
    override val name: String = "ADOPT"

    override val handler: Set[Int] => Future[Seq[Pet]] = { ids =>
      for {
        pets <- Pets.findByIds(ids).run
        adoptedPets = pets.map(_.copy(adopted = true))
        _ <- Pets.updateAll(adoptedPets).run
      } yield adoptedPets
    }
  }

}
