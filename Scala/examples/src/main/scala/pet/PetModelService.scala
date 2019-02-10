package pet

import cats.Applicative
import cats.implicits.catsSyntaxApply
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import pet.PetModel.Pet
import pet.PetModelService.AdoptForm
import pet.Pets.{pets, PetsTable}
import scalest.admin.action.{Action, ActionSuccess}
import scalest.admin.{action, FutureToEffect}
import scalest.service.GenId
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile

import scala.concurrent.ExecutionContext

class PetModelService[F[_]: FutureToEffect: Applicative](
  implicit val dc: DatabaseConfig[H2Profile],
  val ec: ExecutionContext,
) extends SlickModelService[F, Pet, String, PetsTable] {
  import dc.profile.api._
  override val genId: GenId[String] = GenId.genUUID
  override val tableQuery: TableQuery[PetsTable] = pets
  override val actions: List[action.Action[F, _]] = List(Adopt, Disadopt)
  override def idParam: IdParam = IdParam(_.id)

  //Actions
  def Adopt: Action[F, AdoptForm] =
    Action[F, AdoptForm]("ADOPT") { form =>
      update(form.id, _.copy(adopted = true, tags = Seq(form.tag))) *> ActionSuccess.pure[F]
    }

  def Disadopt: Action[F, AdoptForm] =
    Action[F, AdoptForm]("DISADOPT") { form =>
      update(form.id, _.copy(adopted = false, tags = Seq(form.tag))) *> ActionSuccess.pure[F]
    }
}

object PetModelService {
  case class AdoptForm(id: String, tag: String)

  object AdoptForm {
    implicit val decoder: Decoder[AdoptForm] = deriveDecoder[AdoptForm]
  }
  def apply[F[_]](implicit instance: PetModelService[F]): PetModelService[F] = instance
}
