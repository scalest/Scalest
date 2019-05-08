package pet

import cats.effect.IO
import pet.PetModel.Pet
import scalest.admin._

import scala.concurrent.Future

trait PetsModelAdmin {
  def AdoptFuture(petRepository: PetRepository[Future]): ModelAction[Future, Pet, String] =
    ModelAction("ADOPT")(i => petRepository.update(i.toSeq, _.copy(adopted = true)))

  def AdoptIO(petRepository: PetRepository[IO]): ModelAction[IO, Pet, String] =
    ModelAction("ADOPT")(i => petRepository.update(i.toSeq, _.copy(adopted = true)))
}
